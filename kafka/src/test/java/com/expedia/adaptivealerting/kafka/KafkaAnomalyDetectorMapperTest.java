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

import com.expedia.adaptivealerting.anomdetect.util.JmxReporterFactory;
import com.expedia.adaptivealerting.anomdetect.detect.MappedMetricData;
import com.expedia.adaptivealerting.anomdetect.mapper.Detector;
import com.expedia.adaptivealerting.anomdetect.mapper.DetectorMapper;
import com.expedia.adaptivealerting.kafka.serde.MappedMetricDataJsonSerde;
import com.expedia.adaptivealerting.kafka.serde.MetricDataJsonSerde;
import com.expedia.adaptivealerting.kafka.util.TestObjectMother;
import com.expedia.metrics.MetricData;
import com.expedia.metrics.MetricDefinition;
import com.typesafe.config.Config;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.streams.TopologyTestDriver;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.test.ConsumerRecordFactory;
import org.apache.kafka.streams.test.OutputVerifier;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Unit test for {@link KafkaAnomalyDetectorMapper}. See
 * https://kafka.apache.org/20/documentation/streams/developer-guide/testing.html
 */
@Slf4j
public final class KafkaAnomalyDetectorMapperTest {
    private static final String KAFKA_KEY = "some-kafka-key";
    private static final String INPUT_TOPIC = "metrics";
    private static final String DEFAULT_OUTPUT_TOPIC = "mapped-metrics";
    private static final String INVALID_INPUT_VALUE = "invalid-input-value";
    private static final String STATE_STORE_NAME = "es-request-buffer";
    private static final String INTERNAL_DETECTOR_CONSUMER_ID = "ad-manager";
    private static final String EXTERNAL_DETECTOR_CONSUMER_ID = "external-detector";

    @Mock
    private DetectorMapper mapper;

    @Mock
    private StreamsAppConfig saConfig;

    @Mock
    private Config tsConfig;

    // Test objects
    private MetricData metricData;
    private MappedMetricData mappedMetricData;
    private MappedMetricData externalDetectorMappedMetricData;

    // Test machinery
    private TopologyTestDriver logAndFailDriver;
    private TopologyTestDriver logAndContinueDriver;
    private ConsumerRecordFactory<String, MetricData> metricDataFactory;
    private ConsumerRecordFactory<String, String> stringFactory;
    private StringDeserializer stringDeser;
    private Deserializer<MappedMetricData> mmdDeser;
    private Detector detector;
    private Detector externalDetector;
    private KeyValueStore<String, MetricData> kvStore;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        initConfig();
        initTestObjects();
        initTestMachinery();
    }

    @Test
    public void testMetricDataToMappedMetricData() {
        initLogAndFail();
        when(mapper.getDetectorsFromCache(any(MetricDefinition.class)))
                .thenReturn(Collections.singletonList(detector));

        logAndFailDriver.pipeInput(metricDataFactory.create(INPUT_TOPIC, KAFKA_KEY, metricData));

        // The streams app remaps the key to the detector UUID. [WLW]
        val outputRecord = logAndFailDriver.readOutput(DEFAULT_OUTPUT_TOPIC, stringDeser, mmdDeser);
        log.trace("outputRecord={}", outputRecord);
        val outputKafkaKey = mappedMetricData.getDetectorUuid().toString();
        OutputVerifier.compareKeyValue(outputRecord, outputKafkaKey, mappedMetricData);
        assertEquals(DEFAULT_OUTPUT_TOPIC, outputRecord.topic());
        logAndFailDriver.close();
    }

    @Test
    public void testMetricDataToExternalDetectorMappedMetricData() {
        initLogAndFail();
        when(mapper.getDetectorsFromCache(any(MetricDefinition.class)))
                .thenReturn(Collections.singletonList(externalDetector));

        logAndFailDriver.pipeInput(metricDataFactory.create(INPUT_TOPIC, KAFKA_KEY, metricData));
        String externalDetectorKafkaTopic = DEFAULT_OUTPUT_TOPIC + "-" + EXTERNAL_DETECTOR_CONSUMER_ID;
        val outputRecord = logAndFailDriver.readOutput(externalDetectorKafkaTopic, stringDeser, mmdDeser);
        val outputKafkaKey = externalDetectorMappedMetricData.getDetectorUuid().toString();
        log.info("outputRecord={}", outputRecord);
        OutputVerifier.compareKeyValue(outputRecord, outputKafkaKey, externalDetectorMappedMetricData);
        assertEquals(externalDetectorKafkaTopic, outputRecord.topic());
        logAndFailDriver.close();
    }

    /**
     * Addresses bug https://github.com/ExpediaDotCom/adaptive-alerting/issues/253
     * See also https://stackoverflow.com/questions/51136942/how-to-handle-serializationexception-after-deserialization
     */
    @Test
    public void nullOnDeserExceptionWithLogAndFailDriver() {
        initLogAndFail();
        nullOnDeserException(logAndFailDriver);
        logAndFailDriver.close();
    }

    /**
     * Addresses bug https://github.com/ExpediaDotCom/adaptive-alerting/issues/253
     * See also https://stackoverflow.com/questions/51136942/how-to-handle-serializationexception-after-deserialization
     */
    @Test
    public void nullOnDeserExceptionWithLogAndContinueDriver() {
        initLogAndContinue();
        nullOnDeserException(logAndContinueDriver);
        logAndContinueDriver.close();
    }


    @Test
    public void shouldPutInStoreForFirstInput() {
        initLogAndContinue();

        logAndContinueDriver.pipeInput(metricDataFactory.create(INPUT_TOPIC, "key-1", TestObjectMother.metricData()));
        kvStore = logAndContinueDriver.getKeyValueStore(STATE_STORE_NAME);
        assertEquals(kvStore.approximateNumEntries(), 1);

        val outputRecord = logAndContinueDriver.readOutput(DEFAULT_OUTPUT_TOPIC, stringDeser, mmdDeser);
        Assert.assertNull(outputRecord);
        logAndContinueDriver.close();
    }

    @Test
    public void shouldPutInStoreDifferentEntriesForSameKey() {
        initLogAndContinue();

        logAndContinueDriver.pipeInput(metricDataFactory.create(INPUT_TOPIC, "key-1", TestObjectMother.metricData()));
        logAndContinueDriver.pipeInput(metricDataFactory.create(INPUT_TOPIC, "key-1", TestObjectMother.metricData()));
        kvStore = logAndContinueDriver.getKeyValueStore(STATE_STORE_NAME);
        assertEquals(kvStore.approximateNumEntries(), 2);

        logAndContinueDriver.close();
    }

    @Test
    public void shouldOutputResultWhenReceivedMoreEntriesThanOptimumBatchSize() {
        initLogAndContinue();

        when(mapper.getDetectorsFromCache(any(MetricDefinition.class)))
                .thenReturn(Collections.singletonList(detector));
        when(mapper.optimalBatchSize()).thenReturn(2);

        logAndContinueDriver.pipeInput(metricDataFactory.create(INPUT_TOPIC, "key-1", TestObjectMother.metricData()));
        logAndContinueDriver.pipeInput(metricDataFactory.create(INPUT_TOPIC, "key-1", TestObjectMother.metricData()));
        kvStore = logAndContinueDriver.getKeyValueStore(STATE_STORE_NAME);
        val outputRecord = logAndContinueDriver.readOutput(DEFAULT_OUTPUT_TOPIC, stringDeser, mmdDeser);

        OutputVerifier.compareKeyValue(outputRecord, detector.getUuid().toString(), mappedMetricData);
        assertEquals(kvStore.approximateNumEntries(), 0);

        logAndContinueDriver.close();
    }


    private void initConfig() {
        when(saConfig.getTypesafeConfig()).thenReturn(tsConfig);
        when(saConfig.getInputTopic()).thenReturn(INPUT_TOPIC);
        when(saConfig.getOutputTopic()).thenReturn(DEFAULT_OUTPUT_TOPIC);
    }

    private void initTestObjects() {
        this.metricData = TestObjectMother.metricData();
        UUID uuid = UUID.randomUUID();
        this.detector = new Detector(INTERNAL_DETECTOR_CONSUMER_ID, uuid);
        this.externalDetector = new Detector(EXTERNAL_DETECTOR_CONSUMER_ID, uuid);
        this.mappedMetricData = TestObjectMother.mappedMetricData(metricData, INTERNAL_DETECTOR_CONSUMER_ID, uuid);
        this.externalDetectorMappedMetricData = TestObjectMother.mappedMetricData(metricData, EXTERNAL_DETECTOR_CONSUMER_ID, uuid);
    }

    private void initLogAndFail() {
        // Topology test drivers
        val topology = new KafkaAnomalyDetectorMapper(saConfig, mapper, new JmxReporterFactory()).buildTopology();
        this.logAndFailDriver = TestObjectMother.topologyTestDriver(topology, MetricDataJsonSerde.class, false);
    }

    private void initLogAndContinue() {
        // Topology test drivers
        val topology = new KafkaAnomalyDetectorMapper(saConfig, mapper, new JmxReporterFactory()).buildTopology();
        this.logAndContinueDriver = TestObjectMother.topologyTestDriver(topology, MetricDataJsonSerde.class, true);
    }

    private void initTestMachinery() {

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
        val record = driver.readOutput(DEFAULT_OUTPUT_TOPIC, stringDeser, mmdDeser);
        Assert.assertNull(record);
    }
}
