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

import com.expedia.adaptivealerting.core.data.MappedMetricData;
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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PreDestroy;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

// TODO Extract the domain logic and move it to the notifier module. [WLW]

@Component
@Slf4j
public class Notifier implements ApplicationListener<ApplicationReadyEvent> {

    private long TIME_OUT = 10_000;
    private final ObjectMapper objectMapper;
    private final NotifierConfig notifierConfig;
    private final RestTemplate restTemplate;
    private final String webhookUrl;

    final AtomicBoolean running = new AtomicBoolean(); // Visible for testing

    @Autowired
    public Notifier(NotifierConfig notifierConfig, ObjectMapper objectMapper, RestTemplate restTemplate) {
        this.notifierConfig = notifierConfig;
        this.objectMapper = objectMapper;
        this.restTemplate = restTemplate;
        this.webhookUrl = notifierConfig.getWebhookUrl();
    }

    @PreDestroy
    public void stopLooperThread() {
        running.set(false);
    }

    /**
     * This launches a thread to run the notify loop. This prevents a several second hang, or worse
     * crash if zookeeper isn't running, yet.
     */
    @Override
    public void onApplicationEvent(ApplicationReadyEvent applicationReadyEvent) {
        if (!running.compareAndSet(false, true)) return; // already running

        Thread notifyLoop = new Thread(this::loopUntilShutdown);
        notifyLoop.setName("adaptivealerting notify loop");
        notifyLoop.setDaemon(true);
        notifyLoop.start();
    }

    private void loopUntilShutdown() {
        try (KafkaConsumer<String, MappedMetricData> kafkaConsumer =
                     new KafkaConsumer<>(notifierConfig.getKafkaConsumerConfig())) {
            kafkaConsumer.subscribe(Arrays.asList(notifierConfig.getKafkaTopic()));

            while (running.get()) {
                processAlerts(kafkaConsumer);
            }
        }
    }

    private void processAlerts(KafkaConsumer<String, MappedMetricData> kafkaConsumer) {
        ConsumerRecords<String, MappedMetricData> consumerRecords = kafkaConsumer.poll(TIME_OUT);
        consumerRecords.forEach(record -> {
            buildJson(record.value()).ifPresent(json -> {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<String> entity = new HttpEntity<>(json, headers);
                try {
                    ResponseEntity responseEntity =
                            restTemplate.exchange(webhookUrl, HttpMethod.POST, entity, ResponseEntity.class);
                    if (responseEntity.getStatusCode().is2xxSuccessful()) {
                        MetricsMonitor.notification_success.mark();
                    } else {
                        MetricsMonitor.notification_failure.mark();
                    }
                } catch (HttpClientErrorException ex) {
                    MetricsMonitor.notification_failure.mark();
                    log.error("Webhook Url invocation failed", ex);
                }
            });
        });
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
