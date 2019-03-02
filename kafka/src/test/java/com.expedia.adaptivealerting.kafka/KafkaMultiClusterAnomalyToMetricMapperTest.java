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

import com.expedia.adaptivealerting.core.data.MappedMetricData;
import com.expedia.adaptivealerting.kafka.util.TestObjectMother;
import com.expedia.metrics.MetricData;
import com.expedia.metrics.MetricDefinition;
import com.expedia.metrics.TagCollection;
import com.expedia.metrics.jackson.MetricsJavaModule;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.github.charithe.kafka.EphemeralKafkaBroker;
import com.github.charithe.kafka.KafkaJunitRule;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;

/**
 * {@link KafkaMultiClusterAnomalyToMetricMapper} unit test.
 */
@Slf4j
public class KafkaMultiClusterAnomalyToMetricMapperTest {
    
    // Anomaly consumer
    private static final String ANOMALY_TOPIC = "anomalies";
    private static final String STRING_DESER = "org.apache.kafka.common.serialization.StringDeserializer";
    private static final String ANOMALY_DESER = "com.expedia.adaptivealerting.kafka.serde.MappedMetricDataJsonDeserializer";
    
    // Metric producer
    private static final String METRIC_TOPIC = "metrics";
    private static final String STRING_SER = "org.apache.kafka.common.serialization.StringSerializer";
    private static final String METRIC_SER = "com.expedia.adaptivealerting.kafka.serde.MetricDataJsonSerializer";
    
    private static final int NUM_MESSAGES = 20;
    
    @ClassRule
    public static KafkaJunitRule kafka = new KafkaJunitRule(EphemeralKafkaBroker.create()).waitForStartup();
    
    // Class under test
    private KafkaMultiClusterAnomalyToMetricMapper a2mMapper;
    
    private ObjectMapper objectMapper;
    
    @Before
    public void setUp() {
        val anomalyConsumer = buildAnomalyConsumer();
        val metricProducer = buildMetricProducer();
        
        this.a2mMapper = new KafkaMultiClusterAnomalyToMetricMapper(
                anomalyConsumer,
                metricProducer,
                ANOMALY_TOPIC,
                METRIC_TOPIC);
        
        this.objectMapper = new ObjectMapper()
                .registerModule(new MetricsJavaModule())
                .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }
    
    @Test
    public void run() throws Exception {
        
        // TODO This mapped metric data represents an anomaly. It's a little confusing because there's a class called
        // AnomalyResult, which seems more like an anomaly. Want to revisit whether we need to put the whole MMD on the
        // anomalies topic, or whether putting an AnomalyResult is sufficient. [WLW]
        val anomaly = TestObjectMother.mappedMetricDataWithAnomalyResult();
        val anomalyJson = objectMapper.writeValueAsString(anomaly);
        
        for (int i = 0; i < NUM_MESSAGES; i++) {
            log.info("Writing anomaly: {}", anomalyJson);
            kafka.helper().produceStrings(ANOMALY_TOPIC, anomalyJson);
        }
        
        val mapperThread = new Thread(a2mMapper);
        mapperThread.start();
        mapperThread.join(5000L);
        a2mMapper.getAnomalyConsumer().wakeup();

        val metrics = kafka.helper().consumeStrings(METRIC_TOPIC, NUM_MESSAGES).get();
        assertEquals(NUM_MESSAGES, metrics.size());

        for (val metric : metrics) {
            log.info("metric={}", metric);
        }
    }

    @Test
    public void runSkipsAnomaliesWithAADetectorUuid() throws Exception {
        val tagsWithDetectorUuid = TestObjectMother.metricTagsWithDetectorUuid();
        val metricDefWithDetectorUuid = new MetricDefinition("some-metric-key", tagsWithDetectorUuid, TagCollection.EMPTY);
        val metricDataWithDetectorUuid = TestObjectMother.metricData(metricDefWithDetectorUuid, 100.0);

        val anomalyWithDetectorUuid = TestObjectMother.mappedMetricDataWithAnomalyResult(metricDataWithDetectorUuid);
        val anomalyWithoutDetectorUuid = TestObjectMother.mappedMetricDataWithAnomalyResult();

        val anomalyWithDetectorUuidJson = objectMapper.writeValueAsString(anomalyWithDetectorUuid);
        val anomalyWithoutDetectorUuidJson = objectMapper.writeValueAsString(anomalyWithoutDetectorUuid);

        // TODO The test passes with 1, 1, but with 2, 4 I get a "duplicate key" error. (See below.)
        val withArr = new String[2];
        val withoutArr = new String[4];

        Arrays.fill(withArr, anomalyWithDetectorUuidJson);
        Arrays.fill(withoutArr, anomalyWithoutDetectorUuidJson);

        // Push onto input topic
        // TODO Figure out why passing the whole array in doesn't work (duplicate key error, see above).
        for (val json : withArr) {
            kafka.helper().produceStrings(ANOMALY_TOPIC, json);
        }
        for (val json : withoutArr) {
            kafka.helper().produceStrings(ANOMALY_TOPIC, json);
        }

        // Run the processor
        val mapperThread = new Thread(a2mMapper);
        mapperThread.start();
        mapperThread.join(5000L);
        a2mMapper.getAnomalyConsumer().wakeup();

        // Read from the output topic.
        // Run on a separate thread so we can join if it blocks forever.
        val metrics = new ArrayList<String>();
        val consumerThread = new Thread(() -> {
            while (true) {
                try {
                    metrics.addAll(kafka.helper().consumeStrings(METRIC_TOPIC, withoutArr.length).get());
                } catch (Exception e) {
                    // Do nothing
                }
            }
        });
        consumerThread.start();
        consumerThread.join(5000L);

        assertEquals(withoutArr.length, metrics.size());

        for (val metric : metrics) {
            log.info("metric={}", metric);
        }
    }

    @Test
    public void runSkipsAnomaliesHavingTagWithNullValue() {
        // TODO Do this after improving runSkipsAnomaliesWithAADetectorUuid()
    }

    private KafkaConsumer<String, MappedMetricData> buildAnomalyConsumer() {
        val config = kafka.helper().consumerConfig();
        config.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, STRING_DESER);
        config.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ANOMALY_DESER);
        return new KafkaConsumer<>(config);
    }
    
    private KafkaProducer<String, MetricData> buildMetricProducer() {
        val config = kafka.helper().producerConfig();
        config.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, STRING_SER);
        config.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, METRIC_SER);
        return new KafkaProducer<>(config);
    }
}
