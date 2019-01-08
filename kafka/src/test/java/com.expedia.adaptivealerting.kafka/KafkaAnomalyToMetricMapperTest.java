/*
 * Copyright 2018 Expedia Group, Inc.
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
import com.expedia.adaptivealerting.core.util.jackson.ObjectMapperUtil;
import com.expedia.adaptivealerting.kafka.serde.MetricDataDeserializer;
import com.expedia.metrics.MetricData;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.streams.TopologyTestDriver;
import org.apache.kafka.streams.test.ConsumerRecordFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.when;

/**
 * Unit test for {@link KafkaAnomalyToMetricMapper}. See
 * https://kafka.apache.org/20/documentation/streams/developer-guide/testing.html
 *
 * @author Willie Wheeler
 */
@Slf4j
public class KafkaAnomalyToMetricMapperTest {
    private static final String KAFKA_KEY = "some-kafka-key";
    private static final String INBOUND_TOPIC = "anomalies";
    private static final String OUTBOUND_TOPIC = "mdm";
    
    @Mock
    private StreamsAppConfig saConfig;
    
    // Test objects
    private MetricData metricData;
    private MappedMetricData mappedMetricData;
    
    // Test machinery
    private TopologyTestDriver logAndFailDriver;
    private ConsumerRecordFactory<String, MappedMetricData> mappedMetricDataFactory;
    private StringDeserializer stringDeserializer;
    private Deserializer<MetricData> metricDataDeserializer;
    
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        initConfig();
        initTestObjects();
        initTestMachinery();
    }
    
    @After
    public void tearDown() {
        logAndFailDriver.close();
    }
    
    @Test
    public void testTransform() {
        
        // FIXME Serializing the MMD works, but the driver fails to deserialize the MMD.
        logAndFailDriver.pipeInput(mappedMetricDataFactory.create(INBOUND_TOPIC, KAFKA_KEY, mappedMetricData));
        
        val outputRecord = logAndFailDriver.readOutput(OUTBOUND_TOPIC, stringDeserializer, metricDataDeserializer);
        log.trace("outputRecord={}", outputRecord);
        
        // TODO
    }
    
    private void initConfig() {
        when(saConfig.getInboundTopic()).thenReturn(INBOUND_TOPIC);
        when(saConfig.getOutboundTopic()).thenReturn(OUTBOUND_TOPIC);
    }
    
    private void initTestObjects() {
        this.metricData = TestObjectMother.metricData();
        
        this.mappedMetricData = TestObjectMother.mappedMetricData(metricData);
        mappedMetricData.setAnomalyResult(TestObjectMother.anomalyResult(metricData));
    
        log.trace("mappedMetricData={}", ObjectMapperUtil.writeValueAsString(new ObjectMapper(), mappedMetricData));
    }
    
    private void initTestMachinery() {
        
        // Topology test drivers
        val topology = new KafkaAnomalyToMetricMapper(saConfig).buildTopology();
        this.logAndFailDriver = TestObjectMother.topologyTestDriver(topology, MappedMetricData.class, false);
        
        // MappedMetricData producer
        this.mappedMetricDataFactory = TestObjectMother.mappedMetricDataFactory();
        
        // MetricData consumer
        // We consume from the outbound topic so we can validate the results.
        this.stringDeserializer = new StringDeserializer();
        this.metricDataDeserializer = new MetricDataDeserializer();
    }
}
