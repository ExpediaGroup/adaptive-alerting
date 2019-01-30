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

import com.expedia.adaptivealerting.anomdetect.DetectorMapper;
import com.expedia.adaptivealerting.core.data.MappedMetricData;
import com.expedia.metrics.MetricData;
import com.typesafe.config.Config;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.streams.TopologyTestDriver;
import org.apache.kafka.streams.errors.StreamsException;
import org.apache.kafka.streams.test.ConsumerRecordFactory;
import org.apache.kafka.streams.test.OutputVerifier;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;

import static com.expedia.adaptivealerting.kafka.KafkaAnomalyDetectorMapper.CK_MODEL_SERVICE_URI_TEMPLATE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Unit test for {@link KafkaAnomalyDetectorMapper}. See
 * https://kafka.apache.org/20/documentation/streams/developer-guide/testing.html
 *
 * @author Willie Wheeler
 */
@Slf4j
public final class KafkaDetectorMapperTest {
    private static final String KAFKA_KEY = "some-kafka-key";
    private static final String INBOUND_TOPIC = "metrics";
    private static final String OUTBOUND_TOPIC = "mapped-metrics";
    
    @Mock
    private DetectorMapper mapper;
    
    @Mock
    private StreamsAppConfig saConfig;
    
    @Mock
    private Config tsConfig;
    
    // Test objects
    private MetricData metricData;
    private MappedMetricData mappedMetricData;
    
    // Test machinery
    private TopologyTestDriver logAndFailDriver;
    private TopologyTestDriver logAndContinueDriver;
    private ConsumerRecordFactory<String, MetricData> metricDataFactory;
    private ConsumerRecordFactory<String, String> stringFactory;
    private StringDeserializer stringDeserializer;
    private Deserializer<MappedMetricData> mmdDeserializer;
    
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
    public void testMetricDataToMappedMetricData() {
        logAndFailDriver.pipeInput(metricDataFactory.create(INBOUND_TOPIC, KAFKA_KEY, metricData));
        
        // The streams app remaps the key to the detector UUID. [WLW]
        val outputRecord = logAndFailDriver.readOutput(OUTBOUND_TOPIC, stringDeserializer, mmdDeserializer);
        log.trace("outputRecord={}", outputRecord);
        val outputKafkaKey = mappedMetricData.getDetectorUuid().toString();
        OutputVerifier.compareKeyValue(outputRecord, outputKafkaKey, mappedMetricData);
    }
    
    /**
     * Addresses bug https://github.com/ExpediaDotCom/adaptive-alerting/issues/253
     */
    @Test(expected = StreamsException.class)
    public void testFailsOnDeserializationException() {
        logAndFailDriver.pipeInput(stringFactory.create(INBOUND_TOPIC, KAFKA_KEY, "invalid_input"));
        logAndFailDriver.readOutput(OUTBOUND_TOPIC, stringDeserializer, mmdDeserializer);
    }
    
    /**
     * Addresses bug https://github.com/ExpediaDotCom/adaptive-alerting/issues/253
     */
    @Test
    public void testContinuesOnDeserializationException() {
        logAndContinueDriver.pipeInput(stringFactory.create(INBOUND_TOPIC, KAFKA_KEY, "invalid_input"));
        logAndContinueDriver.readOutput(OUTBOUND_TOPIC, stringDeserializer, mmdDeserializer);
    }
    
    private void initConfig() {
        when(tsConfig.getString(CK_MODEL_SERVICE_URI_TEMPLATE)).thenReturn("https://example.com/");
        
        when(saConfig.getTypesafeConfig()).thenReturn(tsConfig);
        when(saConfig.getInboundTopic()).thenReturn(INBOUND_TOPIC);
        when(saConfig.getOutboundTopic()).thenReturn(OUTBOUND_TOPIC);
    }
    
    private void initTestObjects() {
        this.metricData = TestObjectMother.metricData();
        this.mappedMetricData = TestObjectMother.mappedMetricData(metricData);
    }
    
    private void initDependencies() {
        when(mapper.map(any(MetricData.class)))
                .thenReturn(Collections.singleton(mappedMetricData));
    }
    
    private void initTestMachinery() {
    
        // Topology test drivers
        val topology = new KafkaAnomalyDetectorMapper(saConfig, mapper).buildTopology();
        this.logAndFailDriver = TestObjectMother.topologyTestDriver(topology, MetricData.class, false);
        this.logAndContinueDriver = TestObjectMother.topologyTestDriver(topology, MetricData.class, true);
        
        // MetricData producer
        // The string record factory is just for experimenting with the drivers.
        // It's not really part of the mapper unit test, but I wanted to see how it works anyway.
        this.metricDataFactory = TestObjectMother.metricDataFactory();
        this.stringFactory = TestObjectMother.stringFactory();
        
        // MappedMetricData consumer
        // We consume the key and value from the outbound topic so we can validate the results.
        this.stringDeserializer = new StringDeserializer();
        this.mmdDeserializer = TestObjectMother.mappedMetricDataDeserializer();
    }
}
