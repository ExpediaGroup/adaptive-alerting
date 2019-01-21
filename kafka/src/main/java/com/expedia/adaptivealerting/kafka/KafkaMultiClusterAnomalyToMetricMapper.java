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

import com.expedia.adaptivealerting.anomdetect.util.AnomalyToMetricTransformer;
import com.expedia.adaptivealerting.core.anomaly.AnomalyResult;
import com.expedia.metrics.MetricData;
import com.expedia.metrics.metrictank.MetricTankIdFactory;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Properties;

/**
 * Maps anomalies to metrics.
 *
 * @author Willie Wheeler
 */
@Slf4j
public class KafkaMultiClusterAnomalyToMetricMapper implements Runnable {
    private static final String CONFIG_PATH = "/config/mc-a2m-mapper.conf";
    
    private final Consumer<String, AnomalyResult> anomalyConsumer;
    private final Producer<String, MetricData> metricProducer;
    
    private final AnomalyToMetricTransformer transformer = new AnomalyToMetricTransformer();
    private final MetricTankIdFactory metricTankIdFactory = new MetricTankIdFactory();
    
    public static void main(String[] args) {
        val config = ConfigFactory.load(CONFIG_PATH);
        val mapper = new KafkaMultiClusterAnomalyToMetricMapper(config);
        mapper.run();
    }
    
    public KafkaMultiClusterAnomalyToMetricMapper(Config config) {
        this.anomalyConsumer = createAnomalyConsumer(config.getConfig("anomaly-consumer"));
        this.metricProducer = createMetricProducer(config.getConfig("metric-producer"));
    }
    
    @Override
    public void run() {
        log.info("Starting KafkaMultiClusterAnomalyToMetricMapper");
        
        try {
            while (true) {
                val anomalyRecords = anomalyConsumer.poll(1000);
                for (val anomalyRecord : anomalyRecords) {
                    val anomalyResult = anomalyRecord.value();
                    val metricData = transformer.transform(anomalyResult);
                    val metricDef = metricData.getMetricDefinition();
                    val metricId = metricTankIdFactory.getId(metricDef);
                    val metricRecord = new ProducerRecord<String, MetricData>(metricId, metricData);
                    metricProducer.send(metricRecord);
                }
            }
        } catch (RuntimeException e) {
            log.error("Stopping KafkaMultiClusterAnomalyToMetricMapper", e);
        } finally {
            anomalyConsumer.close();
            metricProducer.flush();
            metricProducer.close();
        }
    }
    
    private Consumer<String, AnomalyResult> createAnomalyConsumer(Config config) {
        val props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, config.getString("bootstrap.servers"));
        props.put(ConsumerConfig.GROUP_ID_CONFIG, config.getString("group.id"));
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, config.getString("key.deserializer"));
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, config.getString("value.deserializer"));
        return new KafkaConsumer<>(props);
    }
    
    private Producer<String, MetricData> createMetricProducer(Config config) {
        val props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, config.getString("bootstrap.servers"));
        props.put(ProducerConfig.CLIENT_ID_CONFIG, config.getString("client.id"));
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, config.getString("key.serializer"));
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, config.getString("value.serializer"));
        return new KafkaProducer<>(props);
    }
}
