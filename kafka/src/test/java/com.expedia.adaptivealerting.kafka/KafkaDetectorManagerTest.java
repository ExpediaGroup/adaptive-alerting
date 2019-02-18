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
import com.expedia.adaptivealerting.core.anomaly.AnomalyResult;
import com.expedia.adaptivealerting.core.data.MappedMetricData;
import com.expedia.adaptivealerting.kafka.util.TestObjectMother;
import com.expedia.metrics.MetricData;
import com.typesafe.config.Config;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.streams.TopologyTestDriver;
import org.apache.kafka.streams.errors.StreamsException;
import org.apache.kafka.streams.test.ConsumerRecordFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.HashSet;

import static org.mockito.Mockito.when;

/**
 * Unit test for {@link KafkaAnomalyDetectorManager}. See
 * https://kafka.apache.org/20/documentation/streams/developer-guide/testing.html
 *
 * @author Willie Wheeler
 */
@Slf4j
public final class KafkaDetectorManagerTest {
    private static final String KAFKA_KEY = "some-kafka-key";
    private static final String INBOUND_TOPIC = "mapped-metrics";
    private static final String OUTBOUND_TOPIC = "anomalies";
    
    @Mock
    private DetectorManager detectorManager;
    
    @Mock
    private StreamsAppConfig saConfig;
    
    @Mock
    private Config tsConfig;
    
    // Test objects
    private MappedMetricData mappedMetricData;
    private AnomalyResult anomalyResult;
    
    // Test machinery
    private TopologyTestDriver logAndFailDriver;
    private TopologyTestDriver logAndContinueDriver;
    private ConsumerRecordFactory<String, MappedMetricData> mappedMetricDataFactory;
    private ConsumerRecordFactory<String, String> stringFactory;
    private StringDeserializer stringDeserializer;
    private Deserializer<AnomalyResult> anomalyResultDeserializer;
    
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
    
    /**
     * Addresses bug https://github.com/ExpediaDotCom/adaptive-alerting/issues/245
     */
    @Test(expected = StreamsException.class)
    public void failsOnDeserializationException() {
        logAndFailDriver.pipeInput(stringFactory.create(INBOUND_TOPIC, KAFKA_KEY, "invalid_input"));
        logAndFailDriver.readOutput(OUTBOUND_TOPIC, stringDeserializer, anomalyResultDeserializer);
    }
    
    /**
     * Addresses bug https://github.com/ExpediaDotCom/adaptive-alerting/issues/245
     */
    @Test
    public void continuesOnDeserializationException() {
        logAndContinueDriver.pipeInput(stringFactory.create(INBOUND_TOPIC, KAFKA_KEY, "invalid_input"));
        logAndContinueDriver.readOutput(OUTBOUND_TOPIC, stringDeserializer, anomalyResultDeserializer);
    }
    
    private void initConfig() {
        when(saConfig.getTypesafeConfig()).thenReturn(tsConfig);
        when(saConfig.getInboundTopic()).thenReturn(INBOUND_TOPIC);
        when(saConfig.getOutboundTopic()).thenReturn(OUTBOUND_TOPIC);
    }
    
    private void initTestObjects() {
        val metricData = TestObjectMother.metricData();
        this.mappedMetricData = TestObjectMother.mappedMetricData(metricData);
    }
    
    private void initDependencies() {
        when(detectorManager.getDetectorTypes())
                .thenReturn(new HashSet<>(Arrays.asList("constant-detector", "ewma-detector")));
    }
    
    private void initTestMachinery() {
        
        // Topology test drivers
        val topology = new KafkaAnomalyDetectorManager(saConfig, detectorManager).buildTopology();
        this.logAndFailDriver = TestObjectMother.topologyTestDriver(topology, MetricData.class, false);
        this.logAndContinueDriver = TestObjectMother.topologyTestDriver(topology, MetricData.class, true);
        
        // MetricData sources
        this.mappedMetricDataFactory = TestObjectMother.mappedMetricDataFactory();
        this.stringFactory = TestObjectMother.stringFactory();
        
        // MappedMetricData consumers
        this.stringDeserializer = new StringDeserializer();
        this.anomalyResultDeserializer = TestObjectMother.anomalyResultDeserializer();
    }
}
