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

import com.expedia.adaptivealerting.anomdetect.DetectorManager;
import com.expedia.adaptivealerting.anomdetect.detect.MappedMetricData;
import com.expedia.adaptivealerting.anomdetect.detect.breakout.BreakoutDetectorResult;
import com.expedia.adaptivealerting.anomdetect.detect.breakout.algo.edmx.EdmxDetectorResult;
import com.expedia.adaptivealerting.anomdetect.detect.outlier.OutlierDetectorResult;
import com.expedia.adaptivealerting.kafka.util.TestObjectMother;
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
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

@Slf4j
public class KafkaDetectorManagerTest {

    // Metric consumer
    private static final String METRIC_TOPIC = "mapped-metrics";
    private static final String STRING_DESER = "org.apache.kafka.common.serialization.StringDeserializer";
    private static final String METRIC_DESER = "com.expedia.adaptivealerting.kafka.serde.MappedMetricDataJsonSerde$Deser";
    private static final String METRIC_SER = "com.expedia.adaptivealerting.kafka.serde.MappedMetricDataJsonSerde$Ser";

    // Anomaly producer
    private static final String TRACING_ENABLED = "enabled";
    private static final String OUTLIER_TOPIC = "outliers";
    private static final String BREAKOUT_TOPIC = "breakouts";
    private static final String STRING_SER = "org.apache.kafka.common.serialization.StringSerializer";
    private static final String ANOMALY_SER = "com.expedia.adaptivealerting.kafka.serde.MappedMetricDataJsonSerde$Ser";
    private static final String ANOMALY_DESER = "com.expedia.adaptivealerting.kafka.serde.MappedMetricDataJsonSerde$Deser";

    private static final int NUM_OUTLIER_METRICS = 10;
    private static final int NUM_BREAKOUT_METRICS = 5;
    private static final long THREAD_JOIN_MILLIS = 5000L;

    @ClassRule
    public static KafkaJunitRule kafka = new KafkaJunitRule(EphemeralKafkaBroker.create()).waitForStartup();

    private KafkaDetectorManager managerUnderTest;
    private ObjectMapper objectMapper;

    @Mock
    private DetectorManager detectorManager;

    private MappedMetricData outlierMMD;
    private MappedMetricData breakoutMMD;
    private OutlierDetectorResult outlierDetectorResult;
    private BreakoutDetectorResult breakoutDetectorResult;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        initTestObjects();
        initDependencies();

        val metricConsumer = buildMetricConsumer();
        val anomalyProducer = buildAnomalyProducer();

        this.managerUnderTest = new KafkaDetectorManager(
                detectorManager,
                metricConsumer,
                anomalyProducer,
                METRIC_TOPIC,
                OUTLIER_TOPIC,
                BREAKOUT_TOPIC,
                TRACING_ENABLED);

        this.objectMapper = new ObjectMapper()
                .registerModule(new MetricsJavaModule())
                .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Test
    public void testBuildManager() {
        val config = ConfigFactory.load("detector-manager.conf");
        val manager = KafkaDetectorManager.buildManager(config);
        assertNotNull(manager);
        assertEquals(METRIC_TOPIC, manager.getMetricTopic());
        assertEquals(OUTLIER_TOPIC, manager.getOutlierTopic());
        assertEquals(BREAKOUT_TOPIC, manager.getBreakoutTopic());
        assertEquals(TRACING_ENABLED, manager.getTracingEnabled());
    }

    @Test
    public void testRun() throws Exception {
        val outlierMmdJson = objectMapper.writeValueAsString(outlierMMD);
        val breakoutMmdJson = objectMapper.writeValueAsString(breakoutMMD);

        for (int i = 0; i < NUM_OUTLIER_METRICS; i++) {
            log.info("Writing mapped metric data: {}", outlierMmdJson);
            kafka.helper().produceStrings(METRIC_TOPIC, outlierMmdJson);
        }

        for (int i = 0; i < NUM_BREAKOUT_METRICS; i++) {
            log.info("Writing mapped metric data: {}", breakoutMmdJson);
            kafka.helper().produceStrings(METRIC_TOPIC, breakoutMmdJson);
        }

        val managerThread = new Thread(managerUnderTest);
        managerThread.start();
        managerThread.join(THREAD_JOIN_MILLIS);
        managerUnderTest.getMetricConsumer().wakeup();

        // FIXME This hangs if there aren't enough messages to consume. Add a timeout?
        val outliers = kafka.helper().consumeStrings(OUTLIER_TOPIC, NUM_OUTLIER_METRICS).get();
        assertEquals(NUM_OUTLIER_METRICS, outliers.size());

        val breakouts = kafka.helper().consumeStrings(BREAKOUT_TOPIC, NUM_BREAKOUT_METRICS).get();
        assertEquals(NUM_BREAKOUT_METRICS, breakouts.size());

        for (val outlier : outliers) {
            log.info("outlierDetectorResult={}", outlier);
        }
    }

    @Test
    public void testRun_unmappedAnomalies() throws Exception {
        // TODO
    }

    private void initTestObjects() {
        this.outlierMMD = TestObjectMother.mappedMetricData();
        this.breakoutMMD = TestObjectMother.mappedMetricData();
        this.outlierDetectorResult = new OutlierDetectorResult();
        this.breakoutDetectorResult = new EdmxDetectorResult();
    }

    private void initDependencies() {
        when(detectorManager.detect(outlierMMD)).thenReturn(outlierDetectorResult);
        when(detectorManager.detect(breakoutMMD)).thenReturn(breakoutDetectorResult);
    }

    private KafkaConsumer<String, MappedMetricData> buildMetricConsumer() {
        val config = kafka.helper().consumerConfig();
        config.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, STRING_DESER);
        config.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, METRIC_DESER);
        return new KafkaConsumer<>(config);
    }

    private KafkaProducer<String, MappedMetricData> buildAnomalyProducer() {
        val config = kafka.helper().producerConfig();
        config.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, STRING_SER);
        config.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ANOMALY_SER);
        return new KafkaProducer<>(config);
    }
}
