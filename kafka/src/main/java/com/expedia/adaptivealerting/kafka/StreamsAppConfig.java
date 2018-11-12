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

import com.typesafe.config.Config;
import lombok.Getter;
import lombok.val;
import org.apache.kafka.streams.StreamsConfig;

import java.util.Properties;

import static com.expedia.adaptivealerting.core.util.AssertUtil.notNull;

/**
 * Kafka streams application configuration. Consolidates both Typesafe Config and Streams Config.
 *
 * @author Willie Wheeler
 */
public class StreamsAppConfig {
    
    /**
     * Kafka streams configuration key.
     */
    private static final String CK_STREAMS = "streams";
    
    /**
     * Inbound topic configuration key.
     */
    private static final String CK_INBOUND_TOPIC = "inbound-topic";
    
    /**
     * Outbound topic configuration key.
     */
    private static final String CK_OUTBOUND_TOPIC = "outbound-topic";
    
    /**
     * Health status path configuration key.
     */
    private static final String CK_HEALTH_STATUS_PATH = "health.status.path";
    
    @Getter
    private final Config typesafeConfig;
    
    @Getter
    private final StreamsConfig streamsConfig;
    
    @Getter
    private final String inboundTopic;
    
    @Getter
    private final String outboundTopic;
    
    @Getter
    private final String healthStatusPath;
    
    public StreamsAppConfig(Config typesafeConfig) {
        notNull(typesafeConfig, "typesafeConfig can't be null");
        this.typesafeConfig = typesafeConfig;
        this.streamsConfig = toStreamsConfig(typesafeConfig.getConfig(CK_STREAMS));
        this.inboundTopic = typesafeConfig.getString(CK_INBOUND_TOPIC);
        this.outboundTopic = typesafeConfig.getString(CK_OUTBOUND_TOPIC);
        this.healthStatusPath = typesafeConfig.getString(CK_HEALTH_STATUS_PATH);
    }
    
    private StreamsConfig toStreamsConfig(Config config) {
        val props = new Properties();
        config.entrySet().forEach(e -> props.setProperty(e.getKey(), config.getString(e.getKey())));
        return new StreamsConfig(props);
    }
}
