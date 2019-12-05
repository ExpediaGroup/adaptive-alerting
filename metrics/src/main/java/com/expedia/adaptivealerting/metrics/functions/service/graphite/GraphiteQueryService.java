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
package com.expedia.adaptivealerting.metrics.functions.service.graphite;

import com.expedia.adaptivealerting.anomdetect.util.HttpClientWrapper;
import com.expedia.adaptivealerting.metrics.functions.source.MetricFunctionsSpec;
import com.expedia.adaptivealerting.metrics.functions.source.graphite.GraphiteQueryResult;
import com.expedia.adaptivealerting.metrics.functions.source.graphite.ConstructSourceURI;
import com.expedia.metrics.MetricData;
import com.expedia.metrics.MetricDefinition;
import com.expedia.metrics.TagCollection;
import com.typesafe.config.Config;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.fluent.Content;

import java.util.HashMap;

@Slf4j
public class GraphiteQueryService {
    private HttpClientWrapper metricFunctionHttpClient;
    private final String EMPTY_RESULT_FROM_SOURCE = "{}";
    private final String METRICDATA_KEY_FOR_EMPTY_DATA = "aggregator.producer.";
    private final double METRICDATA_DEFAULT_VALUE = 0.0;
    private final String METRIC_SOURCE_KEY = "metric-source";
    /* For now source supported is graphite alone */
    private final String GRAPHITE_SOURCE_KEY = "graphite";
    private final String GRAPHITE_EMPTY_RESULT = "[]";
    private final static String GRAPHITE_KEY_TAG = "name";

    public GraphiteQueryService(HttpClientWrapper httpClientWrapper) {
        metricFunctionHttpClient = httpClientWrapper;
    }


    public MetricData queryMetricSource(Config metricSourceSinkConfig, MetricFunctionsSpec metricFunctionsSpec) {
        /* For now source supported is graphite alone */
        if (metricSourceSinkConfig.getString(METRIC_SOURCE_KEY).equals(GRAPHITE_SOURCE_KEY)) {
            return graphiteMetricData(metricSourceSinkConfig, metricFunctionsSpec);
        }
        else {
            return defaultMetricData(metricFunctionsSpec);
        }
    }

    private String queryGraphiteSource(Config metricSourceSinkConfig, MetricFunctionsSpec metricFunctionsSpec) {
        try {
            ConstructSourceURI constructSourceURI = new ConstructSourceURI();
            String URI = constructSourceURI.getGraphiteURI(metricSourceSinkConfig, metricFunctionsSpec);
            Content graphiteResult = metricFunctionHttpClient.get(URI);
            if (graphiteResult.asString().equals(GRAPHITE_EMPTY_RESULT)) {
                return EMPTY_RESULT_FROM_SOURCE;
            }
            return graphiteResult.asString();
        }
        catch(Exception e) {
            log.error("Exception during reading from metric source", e);
            return EMPTY_RESULT_FROM_SOURCE;
        }
    }

    private MetricData graphiteMetricData(Config metricSourceSinkConfig, MetricFunctionsSpec metricFunctionsSpec) {
        String metricQueryResult = queryGraphiteSource(metricSourceSinkConfig, metricFunctionsSpec);
        GraphiteQueryResult graphiteQueryResult = new GraphiteQueryResult();
        if (!(EMPTY_RESULT_FROM_SOURCE.equals(metricQueryResult) ||
                graphiteQueryResult.validateNullDatapoint(metricQueryResult))) {
            graphiteQueryResult.getGraphiteQueryResultFromJson(metricQueryResult);
            String graphiteKey = graphiteQueryResult.getTags().get(GRAPHITE_KEY_TAG);
            HashMap<String, String> tagsBuilder = new HashMap<>();
            tagsBuilder.putAll(metricFunctionsSpec.getTags());
            tagsBuilder.putAll(graphiteQueryResult.getTags());
            TagCollection tags = new TagCollection(tagsBuilder);
            TagCollection meta = TagCollection.EMPTY;
            MetricDefinition metricDefinition = new MetricDefinition(graphiteKey, tags, meta);
            return new MetricData(metricDefinition, graphiteQueryResult.getDatapoint().getValue(),
                    graphiteQueryResult.getDatapoint().getTimestamp());
        }
        else {
            return defaultMetricData(metricFunctionsSpec);
        }
    }

    private MetricData defaultMetricData(MetricFunctionsSpec metricFunctionsSpec) {
        TagCollection tags = TagCollection.EMPTY;
        TagCollection meta = TagCollection.EMPTY;
        MetricDefinition metricDefinition = new MetricDefinition((METRICDATA_KEY_FOR_EMPTY_DATA +
                metricFunctionsSpec.getFunction()),
                tags, meta);
        return new MetricData(metricDefinition, METRICDATA_DEFAULT_VALUE,
                System.currentTimeMillis()/1000);
    }
}
