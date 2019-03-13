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
import com.expedia.adaptivealerting.core.anomaly.AnomalyResult;
import com.expedia.adaptivealerting.core.data.MappedMetricData;
import com.expedia.adaptivealerting.core.util.ObjectMapperUtil;
import com.expedia.adaptivealerting.kafka.serde.json.AlertJsonDeserializer;
import com.expedia.adaptivealerting.kafka.serde.json.MappedMetricDataJsonSerde;
import com.expedia.adaptivealerting.kafka.util.TestObjectMother;
import com.expedia.alertmanager.model.Alert;
import com.expedia.metrics.MetricData;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.streams.TopologyTestDriver;
import org.apache.kafka.streams.test.ConsumerRecordFactory;
import org.apache.kafka.streams.test.OutputVerifier;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.when;

/**
 * Unit test for {@link KafkaAnomalyToAlertMapper}. See
 * https://kafka.apache.org/20/documentation/streams/developer-guide/testing.html
 */
@Slf4j
public class KafkaAnomalyToAlertMapperTest {
    private static final String KAFKA_KEY = "some-kafka-key";
    private static final String INBOUND_TOPIC = "anomalies";
    private static final String OUTBOUND_TOPIC = "alert";

    @Mock
    private StreamsAppConfig streamsAppConfig;

    //Test Objects
    private Alert alert;
    private MappedMetricData mappedMetricData;
    private AnomalyResult anomalyResult;
    private MetricData metricData;

    //Test machinery
    private TopologyTestDriver logAndFailDriver;
    private ConsumerRecordFactory<String, MappedMetricData> mappedMetricDataFactory;
    private StringDeserializer stringDeserializer;
    private Deserializer<Alert> alertDeserializer;

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
        logAndFailDriver.pipeInput(mappedMetricDataFactory.create(INBOUND_TOPIC, KAFKA_KEY, mappedMetricData));

        val outputRecord = logAndFailDriver.readOutput(OUTBOUND_TOPIC, stringDeserializer, alertDeserializer);
        val expectedKey = mappedMetricData.getDetectorUuid().toString();
        log.trace("Output Record={}", outputRecord.toString());

        OutputVerifier.compareKeyValue(outputRecord, expectedKey, alert);
    }

    private void initConfig() {
        when(streamsAppConfig.getInboundTopic()).thenReturn(INBOUND_TOPIC);
        when(streamsAppConfig.getOutboundTopic()).thenReturn(OUTBOUND_TOPIC);
    }

    private void initTestObjects() {
        this.alert = TestObjectMother.alert();
        this.metricData = TestObjectMother.metricData();
        this.anomalyResult = new AnomalyResult(AnomalyLevel.STRONG);
        this.mappedMetricData = TestObjectMother.mappedMetricData(metricData);
        mappedMetricData.setAnomalyResult(anomalyResult);
        log.trace("alert={}", ObjectMapperUtil.writeValueAsString(new ObjectMapper(), alert));
    }

    private void initTestMachinery() {
        val topology = new KafkaAnomalyToAlertMapper(streamsAppConfig).buildTopology();
        this.logAndFailDriver = TestObjectMother.topologyTestDriver(topology, MappedMetricDataJsonSerde.class, false);
        this.mappedMetricDataFactory = TestObjectMother.mappedMetricDataFactory();
        this.stringDeserializer = new StringDeserializer();
        this.alertDeserializer = new AlertJsonDeserializer();
    }
}
