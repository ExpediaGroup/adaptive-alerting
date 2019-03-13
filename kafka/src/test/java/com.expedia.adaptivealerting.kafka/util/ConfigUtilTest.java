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

import com.typesafe.config.ConfigFactory;
import lombok.val;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.junit.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public final class ConfigUtilTest {
    private static final String BOOTSTRAP_SERVERS = "kafkasvc:9092";
    private static final String GROUP_ID = "my-group";
    private static final String CLIENT_ID = "my-client";
    private static final String KEY_DESER = "org.apache.kafka.common.serialization.StringDeserializer";
    private static final String VALUE_DESER = "com.expedia.adaptivealerting.kafka.serde.json.MappedMetricDataJsonDeserializer";
    private static final String KEY_SER = "org.apache.kafka.common.serialization.StringSerializer";
    private static final String VALUE_SER = "com.expedia.adaptivealerting.kafka.serde.json.MappedMetricDataJsonSerializer";

    @Test
    public void testToConsumerConfig() {
        val config = ConfigFactory.load("consumer.conf");
        val props = ConfigUtil.toConsumerConfig(config);
        assertEquals(BOOTSTRAP_SERVERS, props.getProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG));
        assertEquals(GROUP_ID, props.getProperty(ConsumerConfig.GROUP_ID_CONFIG));
        assertEquals(KEY_DESER, props.getProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG));
        assertEquals(VALUE_DESER, props.getProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG));
    }

    @Test
    public void testToProducerConfig() {
        val config = ConfigFactory.load("producer.conf");
        val props = ConfigUtil.toProducerConfig(config);
        assertEquals(BOOTSTRAP_SERVERS, props.getProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG));
        assertEquals(CLIENT_ID, props.getProperty(ProducerConfig.CLIENT_ID_CONFIG));
        assertEquals(KEY_SER, props.getProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG));
        assertEquals(VALUE_SER, props.getProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG));
    }
}
