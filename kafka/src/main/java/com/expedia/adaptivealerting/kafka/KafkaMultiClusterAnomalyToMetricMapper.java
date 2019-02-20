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

import com.expedia.adaptivealerting.anomdetect.AnomalyToMetricTransformer;
import com.expedia.adaptivealerting.core.anomaly.AnomalyResult;
import com.expedia.adaptivealerting.core.data.MappedMetricData;
import com.expedia.adaptivealerting.kafka.util.ConfigUtil;
import com.expedia.metrics.MetricData;
import com.expedia.metrics.metrictank.MetricTankIdFactory;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.errors.WakeupException;

import java.util.Collections;

// FIXME The input type is wrong. The anomalies topic contains MappedMetricData, not AnomalyResult. [WLW]

/**
 * Maps anomalies to metrics. Note that the input topic actually contains {@link MappedMetricData} rather than
 * {@link AnomalyResult}.
 *
 * @author Willie Wheeler
 */
@Slf4j
public class KafkaMultiClusterAnomalyToMetricMapper implements Runnable {
    private static final String APP_ID = "mc-a2m-mapper";
    private static final String ANOMALY_CONSUMER = "anomaly-consumer";
    private static final String METRIC_PRODUCER = "metric-producer";
    private static final String TOPIC = "topic";
    private static final long POLL_PERIOD = 1000L;
    
    private final AnomalyToMetricTransformer transformer = new AnomalyToMetricTransformer();
    
    // TODO Replace this with the non-MetricTank version. [WLW]
    private final MetricTankIdFactory metricTankIdFactory = new MetricTankIdFactory();
    
    @Getter
    private final Consumer<String, MappedMetricData> anomalyConsumer;
    
    @Getter
    private final Producer<String, MetricData> metricProducer;
    
    private String anomalyTopic;
    private String metricTopic;
    
    public static void main(String[] args) {
        
        // TODO Refactor the loader such that it's not tied to Kafka Streams. [WLW]
        val config = new TypesafeConfigLoader(APP_ID).loadMergedConfig();
        
        val consumerConfig = config.getConfig(ANOMALY_CONSUMER);
        val consumerTopic = consumerConfig.getString(TOPIC);
        val consumerProps = ConfigUtil.toConsumerConfig(consumerConfig);
        val consumer = new KafkaConsumer<String, MappedMetricData>(consumerProps);
        
        val producerConfig = config.getConfig(METRIC_PRODUCER);
        val producerTopic = producerConfig.getString(TOPIC);
        val producerProps = ConfigUtil.toProducerConfig(producerConfig);
        val producer = new KafkaProducer<String, MetricData>(producerProps);
        
        val mapper = new KafkaMultiClusterAnomalyToMetricMapper(consumer, producer, consumerTopic, producerTopic);
        mapper.run();
    }
    
    public KafkaMultiClusterAnomalyToMetricMapper(
            Consumer<String, MappedMetricData> anomalyConsumer,
            Producer<String, MetricData> metricProducer,
            String anomalyTopic,
            String metricTopic) {
        
        this.anomalyConsumer = anomalyConsumer;
        this.metricProducer = metricProducer;
        this.anomalyTopic = anomalyTopic;
        this.metricTopic = metricTopic;
    }
    
    @Override
    public void run() {
        log.info("Starting KafkaMultiClusterAnomalyToMetricMapper");
        anomalyConsumer.subscribe(Collections.singletonList(anomalyTopic));
        
        // See Kafka: The Definitive Guide, pp. 86 ff.
        try {
            while (true) {
                val records = anomalyConsumer.poll(POLL_PERIOD);
                log.trace("numRecords={}", records.count());
                records.forEach(record -> metricProducer.send(toMetricDataRecord(record)));
            }
        } catch (WakeupException e) {
            // Ignore for shutdown
        } finally {
            anomalyConsumer.close();
            metricProducer.flush();
            metricProducer.close();
        }
    }
    
    private ProducerRecord<String, MetricData> toMetricDataRecord(ConsumerRecord<String, MappedMetricData> record) {
        val mappedMetricData = record.value();
        val anomalyResult = mappedMetricData.getAnomalyResult();
        val metricData = transformer.transform(anomalyResult);
        val metricDef = metricData.getMetricDefinition();
        val metricId = metricTankIdFactory.getId(metricDef);
        return new ProducerRecord<>(metricTopic, metricId, metricData);
    }
}
