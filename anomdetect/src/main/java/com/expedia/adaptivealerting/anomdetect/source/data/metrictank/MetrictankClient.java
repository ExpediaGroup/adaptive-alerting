/*
 * Copyright 2018-2019 Expedia Group, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.expedia.adaptivealerting.anomdetect.source.data.metrictank;

import com.expedia.adaptivealerting.anomdetect.util.HttpClientWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.http.client.fluent.Content;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.expedia.adaptivealerting.anomdetect.util.AssertUtil.notNull;

/**
 * <p>
 * Connector for interacting with graphite API. This allows the anomaly detection module to fetch data from graphite for a given set of metrics.
 * Target parameter could just be a metric name or a combination of metric name and graphite function.
 * </p>
 */
@RequiredArgsConstructor
@Slf4j
public class MetrictankClient {

    public static final String FETCH_METRICS_PATH = "/render?from=%d&until=%d&maxDataPoints=%d&format=json&target=%s";

    @NonNull
    private final String baseUri;

    @NonNull
    private final HttpClientWrapper httpClient;

    @NonNull
    private final ObjectMapper objectMapper;

    /**
     * Fetch metric data for a given set of metrics
     *
     * @param from earliest time (in epoch seconds)
     * @param until latest time (in epoch seconds)
     * @param intervalLength number of seconds per bin of data - used to calculate the maximum data points we expect to retrieve
     * @param target metric name or tag with an optional graphite function
     * @return time series for the specified metric
     */
    public List<MetrictankResult> getData(long from, long until, int intervalLength, String target) {

        notNull(from, "from can't be null");
        notNull(until, "until can't be null");
        notNull(target, "target can't be null");

        // Explicitly specify maxDataPoints in case the number of metrics per day required exceeds server's default maxDataPoints limit
        val maxDataPoints = calculateMaxDataPointsPerDay(from, until, intervalLength);
        val uri = String.format(baseUri + FETCH_METRICS_PATH, from, until, maxDataPoints, target);
        log.debug("Sending query to Metrictank target: {}", uri);

        val headers = Collections.singletonMap("x-org-id", "1");
        Content content;
        try {
            content = httpClient.get(uri, headers);
        } catch (IOException e) {
            val message = String.format("Encountered IOException while querying Metrictank target '%s': httpMethod=GET, uri=%s, message=%s",
                    target,
                    uri,
                    e.getMessage());
            throw new MetrictankClientException(message, e);
        }

        List<MetrictankResult> results;
        try {
            results = Arrays.asList(objectMapper.readValue(content.asBytes(), MetrictankResult[].class));
        } catch (IOException e) {
            throw new MetrictankClientException(String.format("IOException while parsing response from Metrictank target: %s", target), e);
        }
        return results;
    }

    private long calculateMaxDataPointsPerDay(long from, long until, int intervalLength) {
        long intervalsPerDay = (until - from) / intervalLength;
        // We add 2 here to allow for boundary conditions where an extra metric before and/or after requested time window may be included in result
        return intervalsPerDay + 2;
    }
}
