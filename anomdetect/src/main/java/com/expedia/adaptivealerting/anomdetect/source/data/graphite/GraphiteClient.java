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
package com.expedia.adaptivealerting.anomdetect.source.data.graphite;

import com.expedia.adaptivealerting.anomdetect.util.HttpClientWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.http.client.fluent.Content;

import java.io.IOException;

import static com.expedia.adaptivealerting.anomdetect.util.AssertUtil.notNull;

/**
 * <p>
 * Connector for interacting with graphite API. This allows the anomaly detection module to fetch data from graphite for a given set of metrics.
 * </p>
 */
@RequiredArgsConstructor
@Slf4j
public class GraphiteClient {

    public final static String FETCH_METRICS_PATH = "/render?from=-%s&format=json&maxDataPoints=%d&target=%s";

    @NonNull
    private final String baseUri;

    @NonNull
    private final HttpClientWrapper httpClient;

    @NonNull
    private final ObjectMapper objectMapper;


    /**
     * Fetch metric data for a given set of metrics
     *
     * @param from          earliest time
     * @param maxDataPoints max no of data points in result
     * @param target        metric name or tag with an optional graphite function
     * @return time series for the specified metric
     */
    public GraphiteResult[] getMetricData(String from, Integer maxDataPoints, String target) {
        notNull(target, "target can't be null");
        // https://graphite/render?from=-7d&format=json&maxDataPoints=2016&target=sortByMaxima(groupByNodes(stats.gauges.adaptive-alerting-metric-mapper.*.NORMALIZED.kafka-message-processor.consumed.count,%20%27sum%27,%200))
        // https://graphite/render?from=-600s&until=now&format=json&target=seriesByTag("name=cpu")|aliasByTags("server","name")

        val uri = String.format(baseUri + FETCH_METRICS_PATH, getValueOrDefault(from, "1d"), getValueOrDefault(maxDataPoints, 2016), target);
        Content content;
        try {
            content = httpClient.get(uri);
        } catch (IOException e) {
            val message = "IOException while calling graphite " + target +
                    ": httpMethod=GET" +
                    ", uri=" + uri +
                    ", message=" + e.getMessage();
            throw new RuntimeException(message, e);
        }
        GraphiteResult[] results;
        try {
            results = objectMapper.readValue(content.asBytes(), GraphiteResult[].class);
        } catch (IOException e) {
            val message = "IOException while reading graphite data " + target;
            throw new RuntimeException(message, e);
        }
        return results;
    }

    private <T> T getValueOrDefault(T value, T defaultValue) {
        return value == null ? defaultValue : value;
    }
}
