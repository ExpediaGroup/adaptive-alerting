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

import com.expedia.adaptivealerting.kafka.serde.MetricDataJsonSerde;
import com.expedia.adaptivealerting.kafka.util.TestObjectMother;
import com.expedia.adaptivealerting.metricprofiler.MetricProfiler;
import com.expedia.metrics.MetricData;
import com.expedia.metrics.metrictank.MetricTankIdFactory;
import com.github.charithe.kafka.EphemeralKafkaBroker;
import com.github.charithe.kafka.KafkaJunitRule;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.streams.TopologyTestDriver;
import org.apache.kafka.streams.test.ConsumerRecordFactory;
import org.apache.kafka.streams.test.OutputVerifier;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@Slf4j
public class KafkaMetricProfilerTest {

    // Consumes from metrics topic and sends metrics to profiled to profile-metrics kafka topic.
    private static final String KAFKA_KEY = "some-kafka-key";
    private static final String INPUT_TOPIC = "metrics";
    private static final String OUTPUT_TOPIC = "profile-metrics";

    @ClassRule
    public static KafkaJunitRule kafka = new KafkaJunitRule(EphemeralKafkaBroker.create()).waitForStartup();

    private MetricData metricData;
    private ConsumerRecordFactory<String, MetricData> metricDataFactory;
    private StringDeserializer stringDeserializer;
    private Deserializer<MetricData> metricDataDeserializer;

    @Mock
    private MetricProfiler metricProfiler;

    @Mock
    private StreamsAppConfig saConfig;

    @Mock
    private Config tsConfig;

    //Test machinery
    private TopologyTestDriver logAndFailDriver;

    private MetricTankIdFactory idFactory = new MetricTankIdFactory();


    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        initConfig();
        initTestMachinery();
        initTestObjects();
    }

    @After
    public void tearDown() {
        logAndFailDriver.close();
    }

    @Test
    public void testTransform() {
        logAndFailDriver.pipeInput(metricDataFactory.create(INPUT_TOPIC, KAFKA_KEY, metricData));
        val outputRecord = logAndFailDriver.readOutput(OUTPUT_TOPIC, stringDeserializer, metricDataDeserializer);
        log.trace("outputRecord={}", outputRecord);
        val expectedKey = idFactory.getId((metricData.getMetricDefinition()));
        OutputVerifier.compareKeyValue(outputRecord, expectedKey, metricData);
    }

    @Test
    public void testBuildMetricProfiler() {
        val config = ConfigFactory.load("metric-profiler.conf");
        val metricProfiler = KafkaMetricProfiler.buildMetricProfiler(config);
        assertNotNull(metricProfiler);
    }

    private void initTestObjects() {
        this.metricData = TestObjectMother.metricData();
    }

    private void initTestMachinery() {
        val topology = new KafkaMetricProfiler(saConfig, metricProfiler).buildTopology();
        this.logAndFailDriver = TestObjectMother.topologyTestDriver(topology, MetricDataJsonSerde.class, false);
        this.metricDataFactory = TestObjectMother.metricDataFactory();
        this.stringDeserializer = new StringDeserializer();
        this.metricDataDeserializer = new MetricDataJsonSerde.Deser();
    }

    private void initConfig() {
        when(saConfig.getTypesafeConfig()).thenReturn(tsConfig);
        when(saConfig.getInputTopic()).thenReturn(INPUT_TOPIC);
        when(saConfig.getOutputTopic()).thenReturn(OUTPUT_TOPIC);
        when(metricProfiler.hasProfilingInfo(any(MetricData.class))).thenReturn(true);
    }

}
