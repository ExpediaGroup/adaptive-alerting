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
import com.expedia.adaptivealerting.anomdetect.MappedMetricData;
import com.expedia.adaptivealerting.anomdetect.outlier.AnomalyLevel;
import com.expedia.adaptivealerting.anomdetect.outlier.AnomalyResult;
import com.expedia.adaptivealerting.kafka.serde.MappedMetricDataJsonSerde;
import com.expedia.adaptivealerting.kafka.util.TestObjectMother;
import com.typesafe.config.Config;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.streams.TopologyTestDriver;
import org.apache.kafka.streams.test.ConsumerRecordFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

/**
 * Unit test for {@link KafkaAnomalyDetectorManager}. See
 * https://kafka.apache.org/20/documentation/streams/developer-guide/testing.html
 */
@Slf4j
public final class KafkaDetectorManagerTest {
    private static final String KAFKA_KEY = "some-kafka-key";
    private static final String INPUT_TOPIC = "mapped-metrics";
    private static final String OUTPUT_TOPIC = "anomalies";
    private static final String INVALID_INPUT_VALUE = "invalid-input-value";

    @Mock
    private DetectorManager manager;

    @Mock
    private StreamsAppConfig saConfig;

    @Mock
    private Config tsConfig;

    // Test machinery
    private TopologyTestDriver logAndFailDriver;
    private TopologyTestDriver logAndContinueDriver;
    private ConsumerRecordFactory<String, String> stringFactory;
    private ConsumerRecordFactory<String, MappedMetricData> metricFactory;
    private StringDeserializer stringDeser;
    private Deserializer<MappedMetricData> anomalyDeser;

    // Test objects
    private MappedMetricData metric_normalAnomaly;
    private MappedMetricData metric_weakAnomaly;
    private MappedMetricData metric_strongAnomaly;
    private MappedMetricData metric_modelWarmup;
    private MappedMetricData metric_unknownAnomaly;
    private MappedMetricData metric_invalid;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        initConfig();
        initTestObjects();
        initDependencies();
        initTestMachinery();
    }

    @After
    public void tearDown() {
        logAndFailDriver.close();
        logAndContinueDriver.close();
    }

    @Test
    public void testPublishesNormalAnomalies() {
        publishesAnomaly(metric_normalAnomaly, AnomalyLevel.NORMAL);
    }

    @Test
    public void testPublishesWeakAnomalies() {
        publishesAnomaly(metric_weakAnomaly, AnomalyLevel.WEAK);
    }

    @Test
    public void testPublishesStrongAnomalies() {
        publishesAnomaly(metric_strongAnomaly, AnomalyLevel.STRONG);
    }

    @Test
    public void testPublishesWarmupAnomalies() {
        publishesAnomaly(metric_modelWarmup, AnomalyLevel.MODEL_WARMUP);
    }

    @Test
    public void testPublishesUnknownAnomalies() {
        publishesAnomaly(metric_unknownAnomaly, AnomalyLevel.UNKNOWN);
    }

    @Test
    public void testDoesNotPublishInvalidAnomaly() {
        doesNotPublishAnomaly(metric_invalid);
    }

    /**
     * Addresses bug https://github.com/ExpediaDotCom/adaptive-alerting/issues/245
     * See also https://stackoverflow.com/questions/51136942/how-to-handle-serializationexception-after-deserialization
     */
    @Test
    public void nullOnDeserExceptionWithLogAndFailDriver() {
        nullOnDeserException(logAndFailDriver);
    }

    /**
     * Addresses bug https://github.com/ExpediaDotCom/adaptive-alerting/issues/245
     * See also https://stackoverflow.com/questions/51136942/how-to-handle-serializationexception-after-deserialization
     */
    @Test
    public void nullOnDeserExceptionWithLogAndContinueDriver() {
        nullOnDeserException(logAndContinueDriver);
    }

    private void initConfig() {
        when(saConfig.getTypesafeConfig()).thenReturn(tsConfig);
        when(saConfig.getInputTopic()).thenReturn(INPUT_TOPIC);
        when(saConfig.getOutputTopic()).thenReturn(OUTPUT_TOPIC);
    }

    private void initTestObjects() {
        this.metric_normalAnomaly = TestObjectMother.mappedMetricData();
        this.metric_weakAnomaly = TestObjectMother.mappedMetricData();
        this.metric_strongAnomaly = TestObjectMother.mappedMetricData();
        this.metric_modelWarmup = TestObjectMother.mappedMetricData();
        this.metric_unknownAnomaly = TestObjectMother.mappedMetricData();
        this.metric_invalid = TestObjectMother.mappedMetricData();
    }

    private void initDependencies() {
        when(manager.detect(metric_normalAnomaly)).thenReturn(new AnomalyResult(AnomalyLevel.NORMAL));
        when(manager.detect(metric_weakAnomaly)).thenReturn(new AnomalyResult(AnomalyLevel.WEAK));
        when(manager.detect(metric_strongAnomaly)).thenReturn(new AnomalyResult(AnomalyLevel.STRONG));
        when(manager.detect(metric_modelWarmup)).thenReturn(new AnomalyResult(AnomalyLevel.MODEL_WARMUP));
        when(manager.detect(metric_unknownAnomaly)).thenReturn(new AnomalyResult(AnomalyLevel.UNKNOWN));
        when(manager.detect(metric_invalid)).thenThrow(new RuntimeException("Classification error"));
    }

    private void initTestMachinery() {

        // Topology test drivers
        val topology = new KafkaAnomalyDetectorManager(saConfig, manager).buildTopology();
        this.logAndFailDriver = TestObjectMother.topologyTestDriver(topology, MappedMetricDataJsonSerde.class, false);
        this.logAndContinueDriver = TestObjectMother.topologyTestDriver(topology, MappedMetricDataJsonSerde.class, true);

        // MetricData sources
        this.stringFactory = TestObjectMother.stringFactory();
        this.metricFactory = TestObjectMother.mappedMetricDataFactory();

        // MappedMetricData consumers
        this.stringDeser = new StringDeserializer();
        this.anomalyDeser = new MappedMetricDataJsonSerde.Deser();
    }

    private void publishesAnomaly(MappedMetricData metric, AnomalyLevel anomalyLevel) {
        val anomalyRecord = getAnomalyRecord(metric);
        val anomaly = anomalyRecord.value();
        assertNotNull(anomaly);
        assertEquals(anomalyLevel, anomaly.getAnomalyResult().getAnomalyLevel());
    }

    @SuppressWarnings("unused")
    private void doesNotPublishAnomaly(MappedMetricData metric) {
        assertNull(getAnomalyRecord(metric));
    }

    private ProducerRecord<String, MappedMetricData> getAnomalyRecord(MappedMetricData metric) {
        val metricRecord = metricFactory.create(INPUT_TOPIC, KAFKA_KEY, metric);
        logAndFailDriver.pipeInput(metricRecord);
        return logAndFailDriver.readOutput(OUTPUT_TOPIC, stringDeser, anomalyDeser);
    }

    private void nullOnDeserException(TopologyTestDriver driver) {
        driver.pipeInput(stringFactory.create(INPUT_TOPIC, KAFKA_KEY, INVALID_INPUT_VALUE));
        val record = driver.readOutput(OUTPUT_TOPIC, stringDeser, anomalyDeser);
        assertNull(record);
    }
}
