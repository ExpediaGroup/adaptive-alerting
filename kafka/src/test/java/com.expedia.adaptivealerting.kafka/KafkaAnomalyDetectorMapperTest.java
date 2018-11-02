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

import com.expedia.adaptivealerting.anomdetect.AnomalyDetectorMapper;
import com.expedia.adaptivealerting.core.data.MappedMetricData;
import com.expedia.adaptivealerting.kafka.serde.JsonPojoDeserializer;
import com.expedia.adaptivealerting.kafka.serde.JsonPojoSerde;
import com.expedia.adaptivealerting.kafka.serde.JsonPojoSerializer;
import com.expedia.metrics.MetricData;
import com.expedia.metrics.MetricDefinition;
import com.typesafe.config.Config;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.TopologyTestDriver;
import org.apache.kafka.streams.test.ConsumerRecordFactory;
import org.apache.kafka.streams.test.OutputVerifier;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Properties;
import java.util.UUID;

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
public final class KafkaAnomalyDetectorMapperTest {
    private static final String INBOUND_TOPIC = "metrics";
    private static final String OUTBOUND_TOPIC = "mapped-metrics";
    
    @Mock
    private AnomalyDetectorMapper mapper;
    
    @Mock
    private StreamsAppConfig streamsAppConfig;
    
    @Mock
    private Config config;
    
    private Properties streamsProps;
    
    // Test objects
    private MetricData metricData;
    private MappedMetricData mappedMetricData;
    
    // Test machinery
    private StringDeserializer stringDeser;
    private JsonPojoDeserializer<MappedMetricData> mmdDeserializer;
    private ConsumerRecordFactory<String, MetricData> recordFactory;
    private TopologyTestDriver testDriver;
    
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
        testDriver.close();
    }
    
    @Test
    public void metricDataToMappedMetricData() {
        val inputKafkaKey = "some-kafka-key";
        testDriver.pipeInput(recordFactory.create(INBOUND_TOPIC, inputKafkaKey, metricData));
        val outputRecord = testDriver.readOutput(OUTBOUND_TOPIC, stringDeser, mmdDeserializer);
        
        // The streams app remaps the key to the detector UUID. [WLW]
        val outputKafkaKey = mappedMetricData.getDetectorUuid().toString();
        OutputVerifier.compareKeyValue(outputRecord, outputKafkaKey, mappedMetricData);
    }
    
    @Test
    public void handlesDeserializationException() {
        // TODO
    }
    
    private void initConfig() {
        when(config.getString(CK_MODEL_SERVICE_URI_TEMPLATE)).thenReturn("https://example.com/");
        
        when(streamsAppConfig.getTypesafeConfig()).thenReturn(config);
        when(streamsAppConfig.getInboundTopic()).thenReturn(INBOUND_TOPIC);
        when(streamsAppConfig.getOutboundTopic()).thenReturn(OUTBOUND_TOPIC);
        
        this.streamsProps = new Properties();
        streamsProps.put(StreamsConfig.APPLICATION_ID_CONFIG, "test");
        streamsProps.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "dummy:1234");
        streamsProps.setProperty(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        streamsProps.setProperty(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, JsonPojoSerde.class.getName());
        streamsProps.setProperty(JsonPojoDeserializer.CK_JSON_POJO_CLASS, MetricData.class.getName());
    
        // TODO Activate this to avoid crashing the app when deserialization fails.
//        streamsProps.setProperty(StreamsConfig.DEFAULT_DESERIALIZATION_EXCEPTION_HANDLER_CLASS_CONFIG, TODO);
    }
    
    private void initTestObjects() {
        val metricDefinition = new MetricDefinition("some-metric-key");
        val now = Instant.now().getEpochSecond();
        this.metricData = new MetricData(metricDefinition, 100.0, now);
        this.mappedMetricData = new MappedMetricData(metricData, UUID.randomUUID(), "some-detector-type");
    }
    
    private void initDependencies() {
        when(mapper.map(any(MetricData.class)))
                .thenReturn(Collections.singleton(mappedMetricData));
    }
    
    private void initTestMachinery() {
        
        // Test driver
        val kafkaMapper = new KafkaAnomalyDetectorMapper(streamsAppConfig, mapper);
        val topology = kafkaMapper.buildTopology();
        this.testDriver = new TopologyTestDriver(topology, streamsProps);
        
        // MetricData source
        val stringSer = new StringSerializer();
        val mdSerializer = new JsonPojoSerializer<MetricData>();
        this.recordFactory = new ConsumerRecordFactory<>(stringSer, mdSerializer);
        
        // MappedMetricData consumer
        this.stringDeser = new StringDeserializer();
        val mmdDeserProps = new HashMap<String, Object>();
        mmdDeserProps.put(JsonPojoDeserializer.CK_JSON_POJO_CLASS, MappedMetricData.class);
        this.mmdDeserializer = new JsonPojoDeserializer<>();
        mmdDeserializer.configure(mmdDeserProps, false);
    }
}
