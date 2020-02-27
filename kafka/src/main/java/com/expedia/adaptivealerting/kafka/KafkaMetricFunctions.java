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
package com.expedia.adaptivealerting.kafka;

import com.expedia.adaptivealerting.kafka.util.ConfigUtil;
import com.expedia.adaptivealerting.metrics.functions.MetricFunctionsTask;
import com.expedia.adaptivealerting.metrics.functions.sink.MetricFunctionsPublish;
import com.expedia.adaptivealerting.metrics.functions.source.MetricFunctionsReader;
import com.expedia.metrics.MetricData;
import com.typesafe.config.Config;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.expedia.adaptivealerting.anomdetect.util.AssertUtil.notNull;

/**
 * Kafka producer app to derive new metrics by querying the backing data store, and then publish them to a Kafka topic
 * for further processing. Aggregation across hosts (e.g., summing request counts across all hosts) is one common use
 * case.
 */
@Slf4j
public class KafkaMetricFunctions implements MetricFunctionsPublish {
    private static final String APP_ID = "aa-metric-functions";
    private static final String INPUT_FUNCTIONS_FILENAME ="functions.txt";
    private static final String INPUT_FILE_PATH = "/config/";
    private static final String METRIC_STORE_KEY = "metric-source-sink";
    private static final String OUTPUT_TOPIC_KEY = "output-topic";
    private static final int NUM_THREADS = 15;

    // FIXME This is misleadingly named: derived metrics need not be aggregations. [WLW]
    private static final String PRODUCER = "aggregator-producer";

    private Config metricStoreConfig;
    private Producer<String, MetricData> producer;

    public static void main(String[] args) {
        val inputFile = INPUT_FILE_PATH + INPUT_FUNCTIONS_FILENAME;
        val functions = MetricFunctionsReader.readFromInputFile(inputFile);
        log.info("Loaded {} metric functions", functions.size());

        if (functions.isEmpty()) {
            log.info("No metric functions to execute. Exiting." );
            System.exit(0);
        }

        for (val spec : functions) {
            log.info("Loaded metric function: {}", spec);
        }

        val config = new TypesafeConfigLoader(APP_ID).loadMergedConfig();
        val metricStoreConfig = config.getConfig(METRIC_STORE_KEY);

        val kafkaMetricFunctions = new KafkaMetricFunctions();
        kafkaMetricFunctions.initPublisher();

        val execService = Executors.newScheduledThreadPool(NUM_THREADS);
        for (val function : functions) {
            val task = new MetricFunctionsTask(metricStoreConfig, function, kafkaMetricFunctions);
            val intervalInSeconds = function.getIntervalInSecs();
            execService.scheduleAtFixedRate(task, 0, intervalInSeconds, TimeUnit.SECONDS);
        }
    }

    @Override
    public void initPublisher() {
        val config = new TypesafeConfigLoader(APP_ID).loadMergedConfig();
        val producerConfig = config.getConfig(PRODUCER);
        val producerProps = ConfigUtil.toProducerConfig(producerConfig);

        this.metricStoreConfig = config.getConfig(METRIC_STORE_KEY);
        this.producer = new KafkaProducer<>(producerProps);
    }

    @Override
    public void publishMetrics(MetricData metricData) {
        notNull(metricData, "metricData can't be null");

        val topic = metricStoreConfig.getString(OUTPUT_TOPIC_KEY);
        val metricKey = metricData.getMetricDefinition().getKey();

        try {
            producer.send(new ProducerRecord<>(topic, metricKey, metricData));
            log.info("Published derived metric: {}", metricKey);
        } catch (Exception e) {
            log.error("Exception while publishing derived metric to Kafka: " + metricKey, e);
        }
    }
}
