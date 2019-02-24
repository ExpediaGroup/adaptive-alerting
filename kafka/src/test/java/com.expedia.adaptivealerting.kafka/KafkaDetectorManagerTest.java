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
import com.expedia.adaptivealerting.core.anomaly.AnomalyLevel;
import com.expedia.adaptivealerting.core.data.MappedMetricData;
import com.expedia.adaptivealerting.kafka.serde.MappedMetricDataJsonDeserializer;
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

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Unit test for {@link KafkaAnomalyDetectorManager}. See
 * https://kafka.apache.org/20/documentation/streams/developer-guide/testing.html
 */
@Slf4j
public final class KafkaDetectorManagerTest {
    private static final String KAFKA_KEY = "some-kafka-key";
    private static final String INBOUND_TOPIC = "mapped-metrics";
    private static final String OUTBOUND_TOPIC = "anomalies";
    private static final String INVALID_VALUE = "invalid-value";
    
    @Mock
    private DetectorManager detectorManager;
    
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
    public void publishesNormalAnomalies() {
        publishesAnomaly(metric_normalAnomaly, AnomalyLevel.NORMAL);
    }
    
    @Test
    public void publishesWeakAnomalies() {
        publishesAnomaly(metric_weakAnomaly, AnomalyLevel.WEAK);
    }
    
    @Test
    public void publishesStrongAnomalies() {
        publishesAnomaly(metric_strongAnomaly, AnomalyLevel.STRONG);
    }
    
    @Test
    public void publishesWarmupAnomalies() {
        publishesAnomaly(metric_modelWarmup, AnomalyLevel.MODEL_WARMUP);
    }
    
    @Test
    public void publishesUnknownAnomalies() {
        publishesAnomaly(metric_unknownAnomaly, AnomalyLevel.UNKNOWN);
    }
    
    /**
     * Addresses bug https://github.com/ExpediaDotCom/adaptive-alerting/issues/245
     * See also https://stackoverflow.com/questions/51136942/how-to-handle-serializationexception-after-deserialization
     */
    @Test
    public void nullOnDeserExceptionWithLogAndFailDriver() {
        logAndFailDriver.pipeInput(stringFactory.create(INBOUND_TOPIC, KAFKA_KEY, INVALID_VALUE));
        val record = logAndFailDriver.readOutput(OUTBOUND_TOPIC, stringDeser, anomalyDeser);
        assertNull(record);
    }
    
    /**
     * Addresses bug https://github.com/ExpediaDotCom/adaptive-alerting/issues/245
     * See also https://stackoverflow.com/questions/51136942/how-to-handle-serializationexception-after-deserialization
     */
    @Test
    public void nullOnDeserExceptionWithLogAndContinueDriver() {
        logAndContinueDriver.pipeInput(stringFactory.create(INBOUND_TOPIC, KAFKA_KEY, INVALID_VALUE));
        val record = logAndContinueDriver.readOutput(OUTBOUND_TOPIC, stringDeser, anomalyDeser);
        assertNull(record);
    }
    
    private void initConfig() {
        when(saConfig.getTypesafeConfig()).thenReturn(tsConfig);
        when(saConfig.getInboundTopic()).thenReturn(INBOUND_TOPIC);
        when(saConfig.getOutboundTopic()).thenReturn(OUTBOUND_TOPIC);
    }
    
    private void initTestObjects() {
        this.metric_normalAnomaly = TestObjectMother.mappedMetricData();
        this.metric_weakAnomaly = TestObjectMother.mappedMetricData();
        this.metric_strongAnomaly = TestObjectMother.mappedMetricData();
        this.metric_modelWarmup = TestObjectMother.mappedMetricData();
        this.metric_unknownAnomaly = TestObjectMother.mappedMetricData();
    }
    
    private void initDependencies() {
        when(detectorManager.hasDetectorType(anyString())).thenReturn(true);
        
        when(detectorManager.classify(metric_normalAnomaly))
                .thenReturn(TestObjectMother.anomalyResult(AnomalyLevel.NORMAL));
        when(detectorManager.classify(metric_weakAnomaly))
                .thenReturn(TestObjectMother.anomalyResult(AnomalyLevel.WEAK));
        when(detectorManager.classify(metric_strongAnomaly))
                .thenReturn(TestObjectMother.anomalyResult(AnomalyLevel.STRONG));
        when(detectorManager.classify(metric_modelWarmup))
                .thenReturn(TestObjectMother.anomalyResult(AnomalyLevel.MODEL_WARMUP));
        when(detectorManager.classify(metric_unknownAnomaly))
                .thenReturn(TestObjectMother.anomalyResult(AnomalyLevel.UNKNOWN));
    }
    
    private void initTestMachinery() {
        
        // Topology test drivers
        val topology = new KafkaAnomalyDetectorManager(saConfig, detectorManager).buildTopology();
        this.logAndFailDriver = TestObjectMother.topologyTestDriver(topology, MappedMetricDataJsonSerde.class, false);
        this.logAndContinueDriver = TestObjectMother.topologyTestDriver(topology, MappedMetricDataJsonSerde.class, true);
        
        // MetricData sources
        this.stringFactory = TestObjectMother.stringFactory();
        this.metricFactory = TestObjectMother.mappedMetricDataFactory();
        
        // MappedMetricData consumers
        this.stringDeser = new StringDeserializer();
        this.anomalyDeser = new MappedMetricDataJsonDeserializer();
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
        val metricRecord = metricFactory.create(INBOUND_TOPIC, KAFKA_KEY, metric);
        logAndFailDriver.pipeInput(metricRecord);
        return logAndFailDriver.readOutput(OUTBOUND_TOPIC, stringDeser, anomalyDeser);
    }
}
