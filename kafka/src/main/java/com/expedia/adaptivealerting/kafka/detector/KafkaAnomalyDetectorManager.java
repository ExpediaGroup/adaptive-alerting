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
import com.expedia.adaptivealerting.kafka.util.AppUtil;
import com.expedia.adaptivealerting.kafka.util.ReflectionUtil;
import com.typesafe.config.Config;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.KStream;

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
        final KStream<String, MappedMpoint> stream = builder.stream(inboundTopic);
        stream
                .map((key, mappedMpoint) -> KeyValue.pair(key, manager.classify(mappedMpoint)))
                .to(outboundTopic);
        return builder;
    }
    
    private static Map<String, AnomalyDetectorFactory> detectorFactories(Config appConfig) {
        final Map<String, AnomalyDetectorFactory> factories = new HashMap<>();
        appConfig.entrySet().forEach(entry -> {
            final String className = entry.getValue().unwrapped().toString();
            final AnomalyDetectorFactory factory = (AnomalyDetectorFactory) ReflectionUtil.newInstance(className);
            factory.init(appConfig);
            factories.put(entry.getKey(), factory);
        });
        return factories;
    }
}
