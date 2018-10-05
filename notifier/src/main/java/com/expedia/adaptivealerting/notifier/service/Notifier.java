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
package com.expedia.adaptivealerting.notifier.service;

import com.codahale.metrics.MetricRegistry;
import com.expedia.adaptivealerting.core.data.MappedMetricData;
import com.expedia.adaptivealerting.notifier.config.AppConfig;
import com.expedia.adaptivealerting.notifier.util.MetricsMonitor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Optional;

@Component
@Slf4j
public class Notifier implements ApplicationListener<ApplicationReadyEvent> {

    private long TIME_OUT = 10_000;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final KafkaConsumer<String, MappedMetricData> kafkaConsumer;
    private final RestTemplate restTemplate;
    private final String webhookUrl;

    @Autowired
    public Notifier(AppConfig appConfig, RestTemplate restTemplate) {
        kafkaConsumer = buildKafkaConsumer(appConfig);
        this.restTemplate = restTemplate;
        this.webhookUrl = appConfig.getWebhookUrl();
    }

    private KafkaConsumer<String, MappedMetricData> buildKafkaConsumer(AppConfig appConfig) {
        KafkaConsumer<String, MappedMetricData> kafkaConsumer = new KafkaConsumer<>(appConfig.getKafkaConsumerConfig());
        kafkaConsumer.subscribe(Arrays.asList(appConfig.getKafkaTopic()));
        return kafkaConsumer;
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent applicationReadyEvent) {
        processAlerts();
    }

    private void processAlerts() {
        while(!ObjectUtils.isEmpty(webhookUrl)) {
            ConsumerRecords<String, MappedMetricData> consumerRecords = kafkaConsumer.poll(TIME_OUT);
            consumerRecords.forEach( record -> {
                buildJson(record.value()).ifPresent(json -> {
                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.APPLICATION_JSON);
                    HttpEntity<String> entity = new HttpEntity<>(json, headers);
                    try {
                        ResponseEntity responseEntity =
                            restTemplate.exchange(webhookUrl, HttpMethod.POST, entity, ResponseEntity.class);
                        if (responseEntity.getStatusCode().is2xxSuccessful()) {
                            MetricsMonitor.notification_success.mark();
                        }
                        else {
                            MetricsMonitor.notification_failure.mark();
                        }
                    } catch (HttpClientErrorException ex) {
                        MetricsMonitor.notification_failure.mark();
                        log.error("Webhook Url invocation failed", ex);
                    }
                });
            });
        }
    }

    private Optional<String> buildJson(MappedMetricData mappedMetricData) {
        try {
            return Optional.ofNullable(objectMapper.writeValueAsString(mappedMetricData));
        } catch (JsonProcessingException e) {
            log.error("Serialization failed", e);
        }
        return Optional.empty();
    }

}
