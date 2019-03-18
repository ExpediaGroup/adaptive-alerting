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

import com.expedia.adaptivealerting.core.anomaly.AnomalyLevel;
import com.expedia.adaptivealerting.core.data.MappedMetricData;
import com.expedia.adaptivealerting.kafka.util.TestObjectMother;
import com.expedia.metrics.MetricData;
import com.expedia.metrics.jackson.MetricsJavaModule;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.github.charithe.kafka.EphemeralKafkaBroker;
import com.github.charithe.kafka.KafkaJunitRule;
import com.typesafe.config.ConfigFactory;
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
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * {@link KafkaAnomalyToMetricMapper} unit test.
 */
@Slf4j
public final class KafkaAnomalyToMetricMapperTest {

    // Anomaly consumer
    private static final String ANOMALY_TOPIC = "anomalies";
    private static final String STRING_DESER = "org.apache.kafka.common.serialization.StringDeserializer";
    private static final String ANOMALY_SER = "com.expedia.adaptivealerting.kafka.serde.MappedMetricDataJsonSerde$Ser";
    private static final String ANOMALY_DESER = "com.expedia.adaptivealerting.kafka.serde.MappedMetricDataJsonSerde$Deser";

    // Metric producer
    private static final String METRIC_TOPIC = "metrics";
    private static final String STRING_SER = "org.apache.kafka.common.serialization.StringSerializer";
    private static final String METRIC_SER = "com.expedia.adaptivealerting.kafka.serde.MetricDataJsonSerde$Ser";
    private static final String METRIC_DESER = "com.expedia.adaptivealerting.kafka.serde.MetricDataJsonSerde$Deser";

    private static final int NUM_MESSAGES = 20;
    private static final long THREAD_JOIN_MILLIS = 5000L;

    @ClassRule
    public static KafkaJunitRule kafka = new KafkaJunitRule(EphemeralKafkaBroker.create()).waitForStartup();

    private KafkaAnomalyToMetricMapper a2mMapperUnderTest;
    private ObjectMapper objectMapper;

    @Before
    public void setUp() {
        val anomalyConsumer = buildAnomalyConsumer();
        val metricProducer = buildMetricProducer();

        this.a2mMapperUnderTest = new KafkaAnomalyToMetricMapper(
                anomalyConsumer,
                metricProducer,
                ANOMALY_TOPIC,
                METRIC_TOPIC
        );

        this.objectMapper = new ObjectMapper()
                .registerModule(new MetricsJavaModule())
                .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Test
    public void coverageOnly() {
        assertNotNull(a2mMapperUnderTest.getAnomalyConsumer());
        assertNotNull(a2mMapperUnderTest.getMetricProducer());
    }

    @Test
    public void testBuildMapper() {
        val config = ConfigFactory.load("a2m-mapper.conf");
        val mapper = KafkaAnomalyToMetricMapper.buildMapper(config);
        assertNotNull(mapper);
        assertEquals(ANOMALY_TOPIC, mapper.getAnomalyTopic());
        assertEquals(METRIC_TOPIC, mapper.getMetricTopic());
    }

    @Test
    public void testRun() throws Exception {

        // TODO This mapped metric data represents an anomaly. It's a little confusing because there's a class called
        // AnomalyResult, which seems more like an anomaly. Want to revisit whether we need to put the whole MMD on the
        // anomalies topic, or whether putting an AnomalyResult is sufficient. [WLW]
        val anomaly = TestObjectMother.mappedMetricDataWithAnomalyResult();
        val anomalyJson = objectMapper.writeValueAsString(anomaly);

        for (int i = 0; i < NUM_MESSAGES; i++) {
            log.info("Writing anomaly: {}", anomalyJson);
            kafka.helper().produceStrings(ANOMALY_TOPIC, anomalyJson);
        }

        val mapperThread = new Thread(a2mMapperUnderTest);
        mapperThread.start();
        mapperThread.join(THREAD_JOIN_MILLIS);
        a2mMapperUnderTest.getAnomalyConsumer().wakeup();

        val metrics = kafka.helper().consumeStrings(METRIC_TOPIC, NUM_MESSAGES).get();
        assertEquals(NUM_MESSAGES, metrics.size());

        for (val metric : metrics) {
            log.info("metric={}", metric);
        }
    }

    @Test
    public void testRunSkipsUnmappedAnomalies() throws Exception {
        val unmapped = new ArrayList<MappedMetricData>();
        unmapped.add(TestObjectMother.mappedMetricData(AnomalyLevel.NORMAL));
        unmapped.add(TestObjectMother.mappedMetricData(AnomalyLevel.MODEL_WARMUP));
        unmapped.add(TestObjectMother.mappedMetricData(AnomalyLevel.UNKNOWN));
        unmapped.add(TestObjectMother.mappedMetricDataWithAnomalyResultAndAADetectorUuid());
        unmapped.add(TestObjectMother.mappedMetricDataWithAnomalyResultAndNullTagValue());
        testRunSkipsUnmappedAnomalies(unmapped);
    }

    private KafkaProducer<String, MappedMetricData> buildAnomalyProducer() {
        val config = kafka.helper().producerConfig();
        config.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, STRING_SER);
        config.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ANOMALY_SER);
        return new KafkaProducer<>(config);
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

    private KafkaConsumer<String, MetricData> buildMetricConsumer() {
        val config = kafka.helper().consumerConfig();
        config.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, STRING_DESER);
        config.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, METRIC_DESER);
        return new KafkaConsumer<>(config);
    }

    private void testRunSkipsUnmappedAnomalies(List<MappedMetricData> unmapped) throws Exception {

        // Number of anomalies that should actually pass through the mapper.
        final int numOk = 5;

        // Build inputs
        val okAnomaly = TestObjectMother.mappedMetricDataWithAnomalyResult();

        // Not sure why this is a map. I expected to just push a list of anomalies, and not
        // have to worry about using unique keys.
        val anomalies = new HashMap<String, MappedMetricData>();
        for (int i = 0; i < unmapped.size(); i++) {
            anomalies.put("head" + i, unmapped.get(i));
        }
        for (int i = 0; i < numOk; i++) {
            anomalies.put("mid" + i, okAnomaly);
        }
        for (int i = 0; i < unmapped.size(); i++) {
            anomalies.put("tail" + i, unmapped.get(i));
        }

        // Push onto input topic
        val anomalyProducer = buildAnomalyProducer();
        kafka.helper().produce(ANOMALY_TOPIC, anomalyProducer, anomalies);

        // Run the processor
        val mapperThread = new Thread(a2mMapperUnderTest);
        mapperThread.start();
        mapperThread.join(THREAD_JOIN_MILLIS);
        a2mMapperUnderTest.getAnomalyConsumer().wakeup();

        // Read from the output topic.
        // Run on a separate thread so we can join if it blocks forever.
        val metricConsumer = buildMetricConsumer();
        val metrics = new ArrayList<MetricData>();
        val consumerThread = new Thread(() -> {
            try {
                val records = kafka.helper().consume(METRIC_TOPIC, metricConsumer, numOk).get();
                for (val record : records) {
                    metrics.add(record.value());
                }
            } catch (Exception e) {
                log.error("Exception", e);
            }
        });
        consumerThread.start();
        consumerThread.join(THREAD_JOIN_MILLIS);

        // Assertions
        assertEquals(numOk, metrics.size());
    }
}
