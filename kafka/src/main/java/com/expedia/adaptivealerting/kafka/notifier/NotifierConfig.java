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
package com.expedia.adaptivealerting.kafka.notifier;

import com.expedia.metrics.jackson.MetricsJavaModule;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.util.Properties;

@Configuration
public class NotifierConfig {

    @Value("${kafka.topic:alerts}")
    private String topic;

    @Value("${kafka.consumer.bootstrap.servers}")
    private String bootstrapServer;

    @Value("${kafka.consumer.group.id:aa_notifier}")
    private String groupId;

    @Value("${kafka.consumer.key.deserializer:org.apache.kafka.common.serialization.StringDeserializer}")
    private String keyDeSerializer;

    @Value("${kafka.consumer.value.deserializer:com.expedia.adaptivealerting.kafka.serde.MappedMetricDataJsonSerde$Deser}")
    private String valueDeSerializer;

    @Value("${kafka.consumer.auto.offset.reset:earliest}")
    private String autoOffsetReset;

    @Value("${kafka.consumer.session.timeout.ms:30000}")
    private String sessionTimeout;

    @Value("${kafka.consumer.heartbeat.interval.ms:10000}")
    private String heartBeatInterval;

    @Value("${kafka.consumer.request.timeout.ms:40000}")
    private String reqTimeout;

    @Value("${webhook.url}")
    private String webhookUrl;

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper().registerModule(new MetricsJavaModule());
    }

    /**
     * Kafka Configs.
     *
     * @return properties
     */
    public Properties getKafkaConsumerConfig() {
        final Properties properties = new Properties();
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, this.bootstrapServer);
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, this.groupId);
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, this.keyDeSerializer);
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, this.valueDeSerializer);
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, this.autoOffsetReset);
        properties.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, this.sessionTimeout);
        properties.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, this.heartBeatInterval);
        properties.put(ConsumerConfig.REQUEST_TIMEOUT_MS_CONFIG, this.reqTimeout);
        return properties;
    }

    public String getKafkaTopic() {
        return this.topic;
    }

    public String getWebhookUrl() {
        return webhookUrl;
    }
}
