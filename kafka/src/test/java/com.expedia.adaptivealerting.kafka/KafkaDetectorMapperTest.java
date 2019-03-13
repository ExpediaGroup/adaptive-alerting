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
import com.expedia.adaptivealerting.kafka.serde.MappedMetricDataJsonSerde;
import com.expedia.adaptivealerting.kafka.serde.MetricDataJsonSerde;
import com.expedia.adaptivealerting.kafka.util.TestObjectMother;
import com.expedia.metrics.MetricData;
import com.typesafe.config.Config;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.streams.TopologyTestDriver;
import org.apache.kafka.streams.test.ConsumerRecordFactory;
import org.apache.kafka.streams.test.OutputVerifier;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Unit test for {@link KafkaAnomalyDetectorMapper}. See
 * https://kafka.apache.org/20/documentation/streams/developer-guide/testing.html
 */
@Slf4j
public final class KafkaDetectorMapperTest {
    private static final String KAFKA_KEY = "some-kafka-key";
    private static final String INPUT_TOPIC = "metrics";
    private static final String OUTPUT_TOPIC = "mapped-metrics";
    private static final String INVALID_INPUT_VALUE = "invalid-input-value";

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
    private StringDeserializer stringDeser;
    private Deserializer<MappedMetricData> mmdDeser;

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
        logAndFailDriver.pipeInput(metricDataFactory.create(INPUT_TOPIC, KAFKA_KEY, metricData));

        // The streams app remaps the key to the detector UUID. [WLW]
        val outputRecord = logAndFailDriver.readOutput(OUTPUT_TOPIC, stringDeser, mmdDeser);
        log.trace("outputRecord={}", outputRecord);
        val outputKafkaKey = mappedMetricData.getDetectorUuid().toString();
        OutputVerifier.compareKeyValue(outputRecord, outputKafkaKey, mappedMetricData);
    }

    /**
     * Addresses bug https://github.com/ExpediaDotCom/adaptive-alerting/issues/253
     * See also https://stackoverflow.com/questions/51136942/how-to-handle-serializationexception-after-deserialization
     */
    @Test
    public void nullOnDeserExceptionWithLogAndFailDriver() {
        nullOnDeserException(logAndFailDriver);
    }

    /**
     * Addresses bug https://github.com/ExpediaDotCom/adaptive-alerting/issues/253
     * See also https://stackoverflow.com/questions/51136942/how-to-handle-serializationexception-after-deserialization
     */
    @Test
    public void nullOnDeserExceptionWithLogAndContinueDriver() {
        nullOnDeserException(logAndContinueDriver);
    }

    private void initConfig() {
        when(saConfig.getTypesafeConfig()).thenReturn(tsConfig);
        when(saConfig.getInboundTopic()).thenReturn(INPUT_TOPIC);
        when(saConfig.getOutboundTopic()).thenReturn(OUTPUT_TOPIC);
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
        this.logAndFailDriver = TestObjectMother.topologyTestDriver(topology, MetricDataJsonSerde.class, false);
        this.logAndContinueDriver = TestObjectMother.topologyTestDriver(topology, MetricDataJsonSerde.class, true);

        // MetricData producer
        // The string record factory is just for experimenting with the drivers.
        // It's not really part of the mapper unit test, but I wanted to see how it works anyway.
        this.metricDataFactory = TestObjectMother.metricDataFactory();
        this.stringFactory = TestObjectMother.stringFactory();

        // MappedMetricData consumer
        // We consume the key and value from the outbound topic so we can validate the results.
        this.stringDeser = new StringDeserializer();
        this.mmdDeser = new MappedMetricDataJsonSerde.Deser();
    }

    private void nullOnDeserException(TopologyTestDriver driver) {
        driver.pipeInput(stringFactory.create(INPUT_TOPIC, KAFKA_KEY, INVALID_INPUT_VALUE));
        val record = driver.readOutput(OUTPUT_TOPIC, stringDeser, mmdDeser);
        Assert.assertNull(record);
    }
}
