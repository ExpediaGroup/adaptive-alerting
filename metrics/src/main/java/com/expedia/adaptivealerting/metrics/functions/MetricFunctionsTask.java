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
package com.expedia.adaptivealerting.metrics.functions;

import com.expedia.adaptivealerting.anomdetect.util.HttpClientWrapper;
import com.expedia.adaptivealerting.metrics.functions.kafka.KafkaMetricFunctionsToAAMetrics;
import com.expedia.adaptivealerting.metrics.functions.util.ConstructSourceURI;
import org.apache.http.client.fluent.Content;
import com.expedia.adaptivealerting.metrics.functions.source.GraphiteQueryResult;
import com.expedia.adaptivealerting.metrics.functions.source.MetricFunctionsSpec;
import com.expedia.metrics.MetricData;
import com.typesafe.config.Config;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MetricFunctionsTask implements Runnable {
    private MetricFunctionsSpec metricFunctionsSpec;
    private Producer<String, MetricData> aggregateProducer;
    private Config metricSourceSinkConfig;
    private final String OUTPUT_TOPIC_KEY_STRING = "output_topic";


    public MetricFunctionsTask (MetricFunctionsSpec metricFunctionsSpec, Producer<String, MetricData> aggregateProducer,
                                Config metricSourceSinkConfig) {
        this.metricFunctionsSpec = metricFunctionsSpec;
        this.aggregateProducer = aggregateProducer;
        this.metricSourceSinkConfig = metricSourceSinkConfig;
    }

    @SuppressWarnings("unchecked")
    public void run() {
        HttpClientWrapper metricFunctionHttpClient = new HttpClientWrapper();
        /* For now source supported is graphite alone */
        GraphiteQueryResult graphiteQueryResult = new GraphiteQueryResult();
        try {
            ConstructSourceURI constructSourceURI = new ConstructSourceURI();
            String URI = constructSourceURI.getGraphiteURI(metricSourceSinkConfig, metricFunctionsSpec);
            Content graphiteResult = metricFunctionHttpClient.get(URI);
            graphiteQueryResult.getGraphiteQueryResultFromJson(graphiteResult.asString());
        }
        catch(Exception e) {
                log.error("Exception during reading from metric source", e);

        }

        try {
            MetricData metricData = KafkaMetricFunctionsToAAMetrics.streamAggregateRecord(graphiteQueryResult, metricFunctionsSpec);
            ProducerRecord aggregateProducerRecord = new ProducerRecord(
                    metricSourceSinkConfig.getString(OUTPUT_TOPIC_KEY_STRING),
                    metricData.getMetricDefinition().getKey(), metricData);
            aggregateProducer.send(aggregateProducerRecord);
            log.info("Record sent for function: {}", metricData.getMetricDefinition().getKey());
        }
        catch (Exception e) {
            log.error("Exception while sending to kafka", e);

        }
    }

}