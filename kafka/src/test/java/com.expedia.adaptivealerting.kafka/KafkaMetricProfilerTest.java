package com.expedia.adaptivealerting.kafka;

import com.expedia.adaptivealerting.kafka.serde.MetricDataJsonSerde;
import com.expedia.adaptivealerting.kafka.util.TestObjectMother;
import com.expedia.adaptivealerting.metricprofiler.MetricProfiler;
import com.expedia.metrics.MetricData;
import com.expedia.metrics.MetricDefinition;
import com.github.charithe.kafka.EphemeralKafkaBroker;
import com.github.charithe.kafka.KafkaJunitRule;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
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
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

@Slf4j
public class KafkaMetricProfilerTest {

    // Consumes from metrics topic and sends metrics to profiled to profile-metrics kafka topic.
    private static final String KAFKA_KEY = "some-kafka-key";
    private static final String INPUT_TOPIC = "metrics";
    private static final String OUTPUT_TOPIC = "profile-metrics";
    private static final String stateStoreName = "profiler-request-buffer";

    @ClassRule
    public static KafkaJunitRule kafka = new KafkaJunitRule(EphemeralKafkaBroker.create()).waitForStartup();

    private ConsumerRecordFactory<String, MetricData> metricDataFactory;
    private StringDeserializer stringDeserializer;
    private Deserializer<MetricData> metricDataDeserializer;

    private MetricData profiledMetricData;
    private MetricDefinition profiledMetricDefinition;

    private MetricData unProfiledMetricData;
    private MetricDefinition unProfiledMetricDefinition;

    private KeyValueStore<String, MetricData> kvStore;

    @Mock
    private MetricProfiler metricProfiler;

    @Mock
    private StreamsAppConfig saConfig;

    @Mock
    private Config tsConfig;

    //Test machinery
    private TopologyTestDriver logAndFailDriver;
    private TopologyTestDriver logAndContinueDriver;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        initConfig();
        initTestMachinery();
        initTestObjects();
    }

    @Test
    public void shouldReturnNullForProfiledMetrics() {
        initLogAndFail();
        when(metricProfiler.getProfilingInfoFromCache(profiledMetricDefinition)).thenReturn(true);
        logAndFailDriver.pipeInput(metricDataFactory.create(INPUT_TOPIC, KAFKA_KEY, profiledMetricData));
        val outputRecord = logAndFailDriver.readOutput(OUTPUT_TOPIC, stringDeserializer, metricDataDeserializer);
        assertNull(outputRecord);
        logAndFailDriver.close();
    }

    @Test
    public void shouldReturnOutputForNonProfiledMetrics() {
        initLogAndFail();
        when(metricProfiler.getProfilingInfoFromCache(unProfiledMetricDefinition)).thenReturn(false);
        logAndFailDriver.pipeInput(metricDataFactory.create(INPUT_TOPIC, KAFKA_KEY, unProfiledMetricData));
        val outputRecord = logAndFailDriver.readOutput(OUTPUT_TOPIC, stringDeserializer, metricDataDeserializer);
        OutputVerifier.compareKeyValue(outputRecord, "some-kafka-key", unProfiledMetricData);
        logAndFailDriver.close();
    }

    @Test
    public void testBuildMetricProfiler() {
        val config = ConfigFactory.load("metric-profiler.conf");
        val metricProfiler = KafkaMetricProfiler.buildMetricProfiler(config);
        assertNotNull(metricProfiler);
    }

    private void initTestObjects() {
        this.profiledMetricDefinition = new MetricDefinition("good-definition", TestObjectMother.metricTags(), TestObjectMother.metricMeta());
        this.profiledMetricData = TestObjectMother.metricData(profiledMetricDefinition, 100.0);
        this.unProfiledMetricDefinition = new MetricDefinition("bad-definition", TestObjectMother.metricTags(), TestObjectMother.metricMeta());
        this.unProfiledMetricData = TestObjectMother.metricData(unProfiledMetricDefinition, 100.0);
    }

    private void initTestMachinery() {

        this.metricDataFactory = TestObjectMother.metricDataFactory();
        this.stringDeserializer = new StringDeserializer();
        this.metricDataDeserializer = new MetricDataJsonSerde.Deser();
    }

    private void initConfig() {
        when(saConfig.getTypesafeConfig()).thenReturn(tsConfig);
        when(saConfig.getInputTopic()).thenReturn(INPUT_TOPIC);
        when(saConfig.getOutputTopic()).thenReturn(OUTPUT_TOPIC);
    }

    private void initLogAndFail() {
        val topology = new KafkaMetricProfiler(saConfig, metricProfiler).buildTopology();
        this.logAndFailDriver = TestObjectMother.topologyTestDriver(topology, MetricDataJsonSerde.class, false);
    }

    private void initLogAndContinue() {
        val topology = new KafkaMetricProfiler(saConfig, metricProfiler).buildTopology();
        this.logAndContinueDriver = TestObjectMother.topologyTestDriver(topology, MetricDataJsonSerde.class, false);
    }
}
