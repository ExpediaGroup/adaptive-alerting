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
package com.expedia.adaptivealerting.metrics.functions.service;

import com.expedia.adaptivealerting.anomdetect.util.HttpClientWrapper;
import com.expedia.adaptivealerting.metrics.functions.source.MetricFunctionsSpec;
import com.expedia.adaptivealerting.metrics.functions.source.graphite.Datapoint;
import com.expedia.adaptivealerting.metrics.functions.source.graphite.GraphiteResult;
import com.expedia.adaptivealerting.metrics.functions.source.graphite.MissingDatapointException;
import com.expedia.metrics.MetricData;
import com.expedia.metrics.MetricDefinition;
import com.expedia.metrics.TagCollection;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.typesafe.config.Config;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.fluent.Content;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class MetricQueryService {
    private HttpClientWrapper metricFunctionHttpClient;
    private Instant instant;
    // only 'graphite' is currently supported
    private final String METRIC_SOURCE_KEY = "metric-source";

    private final static String IS_GRAPHITE_SERVER_METRICTANK_KEY = "is-graphite-server-metrictank";
    private final static String GRAPHITE_SERVER_METRICTANK = "metrictank";

    private final String GRAPHITE_URL_TEMPLATE_KEY = "urlTemplate";

    public MetricQueryService() {
        this(new HttpClientWrapper(), Instant.now());
    }

    public MetricQueryService(HttpClientWrapper httpClientWrapper, Instant instantWrapper) {
        metricFunctionHttpClient = httpClientWrapper;
        instant = instantWrapper;
    }

    public MetricData queryMetricSource(Config metricSourceSinkConfig, MetricFunctionsSpec metricFunctionsSpec)
            throws IllegalStateException, MetricQueryServiceException {
        String metricSourceName = metricSourceSinkConfig.getString(METRIC_SOURCE_KEY);
        switch (metricSourceName) {
            case "graphite":
                return queryGraphite(metricSourceSinkConfig, metricFunctionsSpec);
            default:
                throw new IllegalStateException(String.format("Unknown metric source '%s'.", metricSourceName));
        }
    }

    private MetricData queryGraphite(Config metricSourceSinkConfig, MetricFunctionsSpec metricFunctionsSpec)
            throws MetricQueryServiceException {
        Boolean success = true;
        String graphiteUrl = "";
        String logTimestamp = "";
        String logValue = "";
        String exceptionName = "";
        String errorMessage = "";
        try {

            String graphiteFunction = metricFunctionsSpec.getFunction();
            int intervalInSecs = metricFunctionsSpec.getIntervalInSecs();
            long currentTimestamp = instant.getEpochSecond();
            long snappedCurrentTimestamp = currentTimestamp - (currentTimestamp % intervalInSecs);

            String graphiteUrlTemplate = metricSourceSinkConfig.getString(GRAPHITE_URL_TEMPLATE_KEY);

            // Subtract 1 second from 'from' and 'until' timestamps to get complete data for
            // the interval -
            // otherwise Graphite gives incomplete data.
            long until = snappedCurrentTimestamp - 1;
            long from = until - intervalInSecs;

            graphiteUrl = String.format("%s%s&from=%d&until=%d", graphiteUrlTemplate, graphiteFunction, from, until);
            Map<String, String> headers = Collections.emptyMap();
            if (metricSourceSinkConfig.getString(IS_GRAPHITE_SERVER_METRICTANK_KEY)
                    .equals(GRAPHITE_SERVER_METRICTANK)) {
                /* default metrictank orgId */
                headers = Collections.singletonMap("x-org-id", "1");
            }
            Content graphiteResponse = metricFunctionHttpClient.get(graphiteUrl, headers);
            ObjectMapper objectMapper = new ObjectMapper();
            List<GraphiteResult> graphiteResults = Arrays
                    .asList(objectMapper.readValue(graphiteResponse.asBytes(), GraphiteResult[].class));
            for (GraphiteResult graphiteResult : graphiteResults) {
                Datapoint datapoint = graphiteResult.getDatapoint();
                logValue = String.valueOf(datapoint.getValue());
                logTimestamp = String.valueOf(datapoint.getTimestamp());

                HashMap<String, String> tagsBuilder = new HashMap<>();
                tagsBuilder.putAll(metricFunctionsSpec.getTags());
                if (metricFunctionsSpec.getMergeTags())
                    tagsBuilder.putAll(graphiteResult.getTags());

                TagCollection tags = new TagCollection(tagsBuilder);
                TagCollection metaTags = TagCollection.EMPTY;
                MetricDefinition metricDefinition = new MetricDefinition(graphiteFunction, tags, metaTags);
                String infoMessage = String.format(
                        "step=queryGraphiteSource,success=%s,exception=%s,url=\"%s\",timestamp=%s,value=%s", success,
                        exceptionName, graphiteUrl, logTimestamp, logValue);
                log.info(infoMessage);
                return new MetricData(metricDefinition, datapoint.getValue(), datapoint.getTimestamp());
            }
            throw new MissingDatapointException();
        } catch (Exception e) {
            success = false;
            exceptionName = e.getClass().getSimpleName();
            errorMessage = String.format("step=queryGraphiteSource,success=%s,exception=%s,url=\"%s\"", success,
                    exceptionName, graphiteUrl);
            throw new MetricQueryServiceException(errorMessage, e);
        }
    }
}
