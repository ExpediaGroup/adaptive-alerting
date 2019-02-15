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
import com.expedia.adaptivealerting.kafka.serde.JsonPojoDeserializer;
import com.expedia.adaptivealerting.kafka.serde.JsonPojoSerde;
import com.expedia.adaptivealerting.kafka.serde.JsonPojoSerializer;
import com.expedia.alertmanager.model.Alert;
import com.expedia.metrics.MetricData;
import com.expedia.metrics.MetricDefinition;
import com.expedia.metrics.TagCollection;
import lombok.val;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.TopologyTestDriver;
import org.apache.kafka.streams.errors.LogAndContinueExceptionHandler;
import org.apache.kafka.streams.test.ConsumerRecordFactory;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Properties;
import java.util.UUID;

/**
 * <p>
 * Utility for creating test objects.
 * </p>
 * <p>
 * Fine to use this for a small number of objects. If this class gets too big then we would want to switch to the
 * builder pattern.
 * </p>
 *
 * @author Willie Wheeler
 */
public final class TestObjectMother {
    
    /**
     * Prevent instantiation.
     */
    private TestObjectMother() {
    }
    
    /**
     * Returns a set of metric tags, valid for Metrictank.
     *
     * @return Metrictank-valid metric tags
     */
    public static TagCollection metricTags() {
        val tags = new HashMap<String, String>();
        
        // Metrics 2.0
        tags.put("mtype", "gauge");
        tags.put("unit", "");
        
        // Metrictank
        tags.put("org_id", "1");
        tags.put("interval", "1");
        
        return new TagCollection(tags);
    }
    
    public static TagCollection metricMeta() {
        val meta = new HashMap<String, String>();
        return new TagCollection(meta);
    }
    
    public static MetricData metricData() {
        val metricDefinition = new MetricDefinition("some-metric-key", metricTags(), metricMeta());
        val now = Instant.now().getEpochSecond();
        return new MetricData(metricDefinition, 100.0, now);
    }
    
    public static MappedMetricData mappedMetricData(MetricData metricData) {
        return new MappedMetricData(metricData, UUID.randomUUID(), "some-detector-type");
    }
    
    public static AnomalyResult anomalyResult(MetricData metricData) {
        val anomalyResult = new AnomalyResult();
        anomalyResult.setDetectorUUID(UUID.randomUUID());
        anomalyResult.setMetricData(metricData);
        anomalyResult.setAnomalyLevel(AnomalyLevel.STRONG);
        return anomalyResult;
    }

    public static Alert alert() {
        val alert = new Alert();
        long timestamp = System.currentTimeMillis();
        alert.setName("some-metric-key");
        HashMap<String,String> annotations = new HashMap<>();
        annotations.put("anomalyLevel","STRONG");
        annotations.put("value","100.0");
        annotations.put("timestamp", String.valueOf(timestamp/1000));
        HashMap<String,String> label = new HashMap<>();
        label.put("metric_key", "some-metric-key"); //Please Note: metric key can override a tag if it has the same name.
        label.put("mtype", "gauge");
        label.put("unit","");
        label.put("org_id", "1");
        label.put("interval","1");
        alert.setLabels(label);
        alert.setAnnotations(annotations);
        return alert;
    }
    
    public static TopologyTestDriver topologyTestDriver(
            Topology topology,
            Class<?> jsonPojoDeserializerClass,
            boolean continueOnDeserException) {
        
        val props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "test");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "dummy:1234");
        props.setProperty(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        props.setProperty(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, JsonPojoSerde.class.getName());
        props.setProperty(JsonPojoDeserializer.CK_JSON_POJO_CLASS, jsonPojoDeserializerClass.getName());
        
        if (continueOnDeserException) {
            props.setProperty(
                    StreamsConfig.DEFAULT_DESERIALIZATION_EXCEPTION_HANDLER_CLASS_CONFIG,
                    LogAndContinueExceptionHandler.class.getName());
        }
        return new TopologyTestDriver(topology, props);
    }
    
    public static ConsumerRecordFactory<String, String> stringFactory() {
        val stringSerializer = new StringSerializer();
        return new ConsumerRecordFactory<>(stringSerializer, stringSerializer);
    }
    
    public static ConsumerRecordFactory<String, MetricData> metricDataFactory() {
        return new ConsumerRecordFactory<>(new StringSerializer(), new JsonPojoSerializer<>());
    }
    
    public static ConsumerRecordFactory<String, MappedMetricData> mappedMetricDataFactory() {
        return new ConsumerRecordFactory<>(new StringSerializer(), new JsonPojoSerializer<>());
    }

    public static JsonPojoDeserializer<MappedMetricData> mappedMetricDataDeserializer() {
        val deserializer = new JsonPojoDeserializer<MappedMetricData>();
        deserializer.configure(
                Collections.singletonMap(JsonPojoDeserializer.CK_JSON_POJO_CLASS, MappedMetricData.class),
                false);
        return deserializer;
    }
    
    public static JsonPojoDeserializer<AnomalyResult> anomalyResultDeserializer() {
        val deserializer = new JsonPojoDeserializer<AnomalyResult>();
        deserializer.configure(
                Collections.singletonMap(JsonPojoDeserializer.CK_JSON_POJO_CLASS, AnomalyResult.class),
                false);
        return deserializer;
    }

    public static JsonPojoDeserializer<Alert> alertDeserializer() {
        val deserializer = new JsonPojoDeserializer<Alert>();
        deserializer.configure(
                Collections.singletonMap(JsonPojoDeserializer.CK_JSON_POJO_CLASS, Alert.class),
                false);
        return deserializer;
    }
}
