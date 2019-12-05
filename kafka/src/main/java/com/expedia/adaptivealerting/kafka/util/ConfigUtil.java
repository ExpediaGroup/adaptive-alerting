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
package com.expedia.adaptivealerting.kafka.util;

import com.typesafe.config.Config;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;

import java.util.Properties;

/**
 * Kafka configuration utilities.
 */
@UtilityClass
public class ConfigUtil {

    public static Properties toConsumerConfig(Config config) {
        val keys = new String[]{
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
            ConsumerConfig.GROUP_ID_CONFIG,
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG
        };
        val props = new Properties();
        copyProps(config, props, keys);
        return props;
    }

    public static Properties toProducerConfig(Config config) {
        val keys = new String[]{
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
            ProducerConfig.CLIENT_ID_CONFIG,
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG
        };
        val props = new Properties();
        copyProps(config, props, keys);
        return props;
    }

    private static void copyProps(Config config, Properties props, String... keys) {
        for (val key : keys) {
            copyProp(config, props, key);
        }
    }

    private static void copyProp(Config config, Properties props, String key) {
        props.setProperty(key, config.getString(key));
    }
}
