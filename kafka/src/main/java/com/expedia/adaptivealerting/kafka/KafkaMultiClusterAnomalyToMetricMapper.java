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
import com.expedia.metrics.MetricData;
import com.expedia.metrics.metrictank.MetricTankIdFactory;
import com.typesafe.config.Config;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Collections;
import java.util.Properties;

/**
 * Maps anomalies to metrics.
 *
 * @author Willie Wheeler
 */
@Slf4j
public class KafkaMultiClusterAnomalyToMetricMapper implements Runnable {
    private static final String APP_ID = "mc-a2m-mapper";
    
    private String anomaliesTopic;
    private String metricsTopic;
    
    private final Consumer<String, AnomalyResult> anomalyConsumer;
    private final Producer<String, MetricData> metricProducer;
    
    private final AnomalyToMetricTransformer transformer = new AnomalyToMetricTransformer();
    private final MetricTankIdFactory metricTankIdFactory = new MetricTankIdFactory();
    
    public static void main(String[] args) {
        // TODO Refactor the loader such that it's not tied to Kafka Streams. [WLW]
        val config = new TypesafeConfigLoader(APP_ID).loadMergedConfig();
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
        anomalyConsumer.subscribe(Collections.singletonList(anomaliesTopic));
        
        // TODO Implement clean shutdown.
        // See Kafka: The Definitive Guide, pp. 86 ff.
        while (true) {
            try {
                anomalyConsumer.poll(100).forEach(record -> {
                    if (record != null) {
                        metricProducer.send(toMetricDataRecord(record));
                    }
                });
            } catch (Exception e) {
                log.error("Exception while processing anomalies", e);
            }
        }
    }
    
    private ProducerRecord<String, MetricData> toMetricDataRecord(ConsumerRecord<String, AnomalyResult> record) {
        val anomalyResult = record.value();
        val metricData = transformer.transform(anomalyResult);
        val metricDef = metricData.getMetricDefinition();
        val metricId = metricTankIdFactory.getId(metricDef);
        return new ProducerRecord<String, MetricData>(metricsTopic, metricId, metricData);
    }
    
    private Consumer<String, AnomalyResult> createAnomalyConsumer(Config config) {
        this.anomaliesTopic = config.getString("topic");
        
        val bootstrapServers = config.getString("bootstrap.servers");
        val groupId = config.getString("group.id");
        val keyDeserClass = config.getString("key.deserializer");
        val valueDeserClass = config.getString("value.deserializer");
        
        log.info("Creating anomaly consumer:");
        log.info("  anomaliesTopic={}", anomaliesTopic);
        log.info("  bootstrapServers={}", bootstrapServers);
        log.info("  groupId={}", groupId);
        log.info("  keyDeserializerClass={}", keyDeserClass);
        log.info("  valueDeserializerClass={}", valueDeserClass);
        
        val props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, keyDeserClass);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, valueDeserClass);
        return new KafkaConsumer<>(props);
    }
    
    private Producer<String, MetricData> createMetricProducer(Config config) {
        this.metricsTopic = config.getString("topic");
        
        val bootstrapServers = config.getString("bootstrap.servers");
        val clientId = config.getString("client.id");
        val keySerClass = config.getString("key.serializer");
        val valueSerClass = config.getString("value.serializer");
        
        log.info("Creating metric producer:");
        log.info("  metricsTopic={}", metricsTopic);
        log.info("  bootstrapServers={}", bootstrapServers);
        log.info("  clientId={}", clientId);
        log.info("  keySerializerClass={}", keySerClass);
        log.info("  valueSerializerClass={}", valueSerClass);
        
        val props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.CLIENT_ID_CONFIG, clientId);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, keySerClass);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, valueSerClass);
        return new KafkaProducer<>(props);
    }
}
