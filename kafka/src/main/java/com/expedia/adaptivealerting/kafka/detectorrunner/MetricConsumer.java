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

package com.expedia.adaptivealerting.kafka.detectorrunner;

import com.expedia.adaptivealerting.anomdetect.detect.MappedMetricData;
import com.expedia.adaptivealerting.kafka.TypesafeConfigLoader;
import com.expedia.adaptivealerting.kafka.util.ConfigUtil;
import com.expedia.metrics.MetricData;
import com.typesafe.config.Config;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Component
public class MetricConsumer implements ApplicationListener<ApplicationReadyEvent> {

    private KafkaConsumer<String, MappedMetricData> kafkaConsumer;
    private static String METRIC_CONSUMER = "metric-consumer";
    private static long POLL_INTERVAL = 1000L;
    private static String APP = "detector-runner";

    final AtomicBoolean running = new AtomicBoolean();

    @Autowired
    private DetectorManager detectorManager;

    @Autowired
    private AnomalyProducer anomalyProducer;

    public MetricConsumer() {
        Config config = new TypesafeConfigLoader(APP).loadMergedConfig();
        Config consumerConfig = config.getConfig(METRIC_CONSUMER);
        Properties metricConsumerProps = ConfigUtil.toConsumerConfig(consumerConfig);
        kafkaConsumer = new KafkaConsumer(metricConsumerProps);
    }

    public void consume() {
        kafkaConsumer.subscribe(Collections.singletonList("mapped-metrics"));
        boolean continueProcessing = true;
        // See Kafka: The Definitive Guide, pp. 86 ff.
        while (continueProcessing) {
            continueProcessing = process(kafkaConsumer, continueProcessing);
        }
    }

    public boolean process(KafkaConsumer kafkaConsumer, boolean continueProcessing) {
        try {
            ConsumerRecords<String, MappedMetricData> metricRecords = kafkaConsumer.poll(POLL_INTERVAL);
            log.info("Read {} metric records from topic={}", metricRecords.count(), "mapped-metrics");
            List<MappedMetricData> mmd = detectorManager.detect(metricRecords);
            for (MappedMetricData mappedMetricData : mmd) {
                if (mappedMetricData != null && mappedMetricData.getMetricData() != null) {
                    MetricData metricData = mappedMetricData.getMetricData();
                    if (metricData.getMetricDefinition() != null && metricData.getMetricDefinition().getKey() != null) {
                        String key = metricData.getMetricDefinition().getKey();
                        ProducerRecord<String, MappedMetricData> producerRecord = new ProducerRecord<>(
                                anomalyProducer.getAnomalyProducerConfig().getString("outbound-topic"),
                                null, 1000L, key, mappedMetricData);
                        anomalyProducer.getProducer().send(producerRecord);
                    }
                }
            }
        } catch (WakeupException e) {
            kafkaConsumer.close();
            anomalyProducer.getProducer().close();
            log.error(e.getLocalizedMessage(), e);
            continueProcessing = false;
        } catch (Exception e) {
            log.error(e.getLocalizedMessage(), e);
            continueProcessing = false;
        }
        return continueProcessing;
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent applicationReadyEvent) {
        if (!running.compareAndSet(false, true)) return; // already running

        Thread consumerLoop = new Thread(this::consume);
        consumerLoop.setName("adaptivealerting detector runner consumer loop");
        consumerLoop.setDaemon(true);
        consumerLoop.start();
    }

    @PreDestroy
    public void stopLooperThread() {
        running.set(false);
    }
}
