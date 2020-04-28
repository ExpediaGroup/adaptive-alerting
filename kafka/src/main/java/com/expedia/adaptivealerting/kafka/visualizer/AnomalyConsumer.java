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

package com.expedia.adaptivealerting.kafka.visualizer;

import com.expedia.adaptivealerting.anomdetect.detect.MappedMetricData;
import com.typesafe.config.Config;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.msgpack.core.annotations.VisibleForTesting;

import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


@Slf4j
public class AnomalyConsumer {

    private KafkaConsumer<String, MappedMetricData> kafkaConsumer;
    private static String TOPIC = "topic";
    private static String METRIC_CONSUMER = "metric-consumer";
    private static long POLL_INTERVAL = 1000L;
    private AnomaliesProcessor anomaliesProcessor;
    private ExecutorService executorService;
    public Config consumerConfig = VisualizerUtility.getConfig(METRIC_CONSUMER);
    public Properties metricProps = VisualizerUtility.getMetricConsumerProps(consumerConfig);

    public AnomalyConsumer() {
        kafkaConsumer = new KafkaConsumer(metricProps);
        anomaliesProcessor = new AnomaliesProcessor();
        executorService = new ThreadPoolExecutor(10, 50, 0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>());
    }

    public void listen() {
        kafkaConsumer.subscribe(Collections.singletonList(consumerConfig.getString(TOPIC)));
        boolean continueProcessing = true;
        // See Kafka: The Definitive Guide, pp. 86 ff.
        while (continueProcessing) {
            continueProcessing = process(kafkaConsumer, continueProcessing);
        }
    }

    public boolean process(KafkaConsumer kafkaConsumer, boolean continueProcessing) {
        try {
            ConsumerRecords<String, MappedMetricData> metricRecords = kafkaConsumer.poll(POLL_INTERVAL);
            log.trace("Read {} metric records from topic={}", metricRecords.count(), consumerConfig.getString(TOPIC));
            anomaliesProcessor.processMetrics(metricRecords, executorService);
        } catch (WakeupException e) {
            kafkaConsumer.close();
            continueProcessing = false;
        } catch (Exception e) {
            log.error(e.getLocalizedMessage());
            e.printStackTrace();
        }

        return continueProcessing;
    }

    @VisibleForTesting
    public void setKafkaConsumer(KafkaConsumer<String, MappedMetricData> kafkaConsumer) {
        this.kafkaConsumer = kafkaConsumer;
    }

    @VisibleForTesting
    public void setExecutorService(ExecutorService executorService) {
        this.executorService = executorService;
    }

    public AnomaliesProcessor getAnomaliesProcessor() {
        return anomaliesProcessor;
    }
}
