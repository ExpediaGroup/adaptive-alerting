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
package com.expedia.adaptivealerting.metrics.functions.kafka;

import com.expedia.adaptivealerting.metrics.functions.source.GraphiteQueryResult;
import com.expedia.adaptivealerting.metrics.functions.source.MetricFunctionsSpec;
import com.expedia.metrics.TagCollection;
import com.expedia.metrics.MetricData;
import com.expedia.metrics.MetricDefinition;
import java.util.HashMap;

public class KafkaMetricFunctionsToAAMetrics {

    private final static String GRAPHITE_KEY_TAG = "name";

    public static MetricData streamAggregateRecord(GraphiteQueryResult graphiteQueryResult, MetricFunctionsSpec metricFunctionsSpec) {
        MetricData metricData = outputMetricData(graphiteQueryResult, metricFunctionsSpec);
        return metricData;
    }

    private static MetricData outputMetricData(GraphiteQueryResult graphiteQueryResult, MetricFunctionsSpec metricFunctionsSpec)
    {
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

}
