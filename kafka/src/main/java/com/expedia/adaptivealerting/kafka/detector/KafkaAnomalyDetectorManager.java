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
package com.expedia.adaptivealerting.kafka.detector;

import com.expedia.adaptivealerting.anomdetect.AnomalyDetectorFactory;
import com.expedia.adaptivealerting.anomdetect.AnomalyDetectorManager;
import com.expedia.adaptivealerting.core.data.MappedMpoint;
import com.expedia.adaptivealerting.kafka.AbstractKafkaApp;
import com.expedia.adaptivealerting.kafka.serde.JsonPojoDeserializer;
import com.expedia.adaptivealerting.kafka.serde.JsonPojoSerializer;
import com.expedia.adaptivealerting.kafka.serde.MpointTimestampExtractor;
import com.expedia.adaptivealerting.kafka.util.AppUtil;
import com.expedia.adaptivealerting.kafka.util.ReflectionUtil;
import com.typesafe.config.Config;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.Consumed;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;

import java.util.HashMap;
import java.util.Map;

import static com.expedia.adaptivealerting.core.util.AssertUtil.notNull;
import static com.expedia.adaptivealerting.kafka.KafkaConfigProps.*;

/**
 * Kafka wrapper around {@link AnomalyDetectorManager}.
 *
 * @author David Sutherland
 * @author Willie Wheeler
 */
public final class KafkaAnomalyDetectorManager extends AbstractKafkaApp {
    private AnomalyDetectorManager manager;
    
    public static void main(String[] args) {
        final Config appConfig = AppUtil.getAppConfig(ANOMALY_DETECTOR_MANAGER);
        final AnomalyDetectorManager manager =
                new AnomalyDetectorManager(detectorFactories(appConfig.getConfig(FACTORIES)));
        new KafkaAnomalyDetectorManager(appConfig, manager).start();
    }
    
    public KafkaAnomalyDetectorManager(Config appConfig, AnomalyDetectorManager manager) {
        super(appConfig);
        notNull(manager, "manager can't be null");
        this.manager = manager;
    }
    
    @Override
    protected StreamsBuilder streamsBuilder() {
        final StreamsBuilder builder = new StreamsBuilder();
        final String inboundTopic = getAppConfig().getString(INBOUND_TOPIC);
        final String outboundTopic = getAppConfig().getString(OUTBOUND_TOPIC);
        final KStream<String, MappedMpoint> stream = builder.stream(inboundTopic, jsonMpointSerde());
        stream
                .map((key, mappedMpoint) -> KeyValue.pair(key, manager.classify(mappedMpoint)))
                .to(outboundTopic, jsonAnomalySerde());
        return builder;
    }
    
    private static Map<String, AnomalyDetectorFactory> detectorFactories(Config appConfig) {
        final Map<String, AnomalyDetectorFactory> factories = new HashMap<>();
        appConfig.entrySet().forEach(entry -> {
            final String factoryClassName = entry.getValue().unwrapped().toString();
            factories.put(entry.getKey(), (AnomalyDetectorFactory) ReflectionUtil.newInstance(factoryClassName));
        });
        return factories;
    }
    
    private Consumed<String, MappedMpoint> jsonMpointSerde() {
        final JsonPojoDeserializer<MappedMpoint> deserializer = new JsonPojoDeserializer<>();
        final Map<String, Object> props = new HashMap<>();
        props.put(JSON_POJO_CLASS, MappedMpoint.class);
        deserializer.configure(props, false);
        
        return Consumed.with(
                new Serdes.StringSerde(),
                Serdes.serdeFrom(new JsonPojoSerializer<>(), deserializer),
                new MpointTimestampExtractor(),
                Topology.AutoOffsetReset.LATEST);
    }
    
    private Produced<String, MappedMpoint> jsonAnomalySerde() {
        // TODO Add StreamPartitioner
        return Produced.with(
                new Serdes.StringSerde(),
                Serdes.serdeFrom(new JsonPojoSerializer<>(), new JsonPojoDeserializer<>()));
    }
}
