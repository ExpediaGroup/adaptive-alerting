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

import com.expedia.adaptivealerting.core.anomaly.AnomalyLevel;
import com.expedia.adaptivealerting.core.anomaly.AnomalyResult;
import com.expedia.adaptivealerting.core.data.MappedMetricData;
import com.expedia.adaptivealerting.notifier.config.AppConfig;
import com.expedia.metrics.MetricData;
import com.expedia.metrics.MetricDefinition;
import com.expedia.metrics.TagCollection;
import com.github.charithe.kafka.EphemeralKafkaBroker;
import com.github.charithe.kafka.KafkaJunitRule;
import com.google.common.collect.ImmutableMap;
import java.time.Instant;
import java.util.UUID;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.apache.kafka.clients.CommonClientConfigs;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.springframework.boot.autoconfigure.context.PropertyPlaceholderAutoConfiguration;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import static com.expedia.metrics.MetricDefinition.MTYPE;
import static com.expedia.metrics.MetricDefinition.UNIT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.springframework.boot.test.util.EnvironmentTestUtils.addEnvironment;

public class NotifierTest {

    @ClassRule public static KafkaJunitRule kafka =
            new KafkaJunitRule(EphemeralKafkaBroker.create()).waitForStartup();
    @ClassRule public static MockWebServer webhook = new MockWebServer();

    AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();

    Notifier notifier;
    AppConfig appConfig;

    @Before
    public void init() {
        addEnvironment(context,
                "kafka.consumer.bootstrap.servers=" + bootstrapServers(),
                "webhook.url=http://localhost:" + webhook.getPort() + "/hook"
        );

        context.register(
            PropertyPlaceholderAutoConfiguration.class, AppConfig.class, Notifier.class);
        context.refresh();
        notifier = context.getBean(Notifier.class);
        appConfig = context.getBean(AppConfig.class);
    }

    @After
    public void close() {
        context.close();
    }

    @Test
    public void applicationReadyEvent_startsNotifier() {
        assertThat(notifier.running).isFalse();

        notifier.onApplicationEvent(mock(ApplicationReadyEvent.class));

        assertThat(notifier.running).isTrue();
    }

    @Test
    public void context_close_stopsNotifier() {
        notifier.onApplicationEvent(mock(ApplicationReadyEvent.class));

        context.close();

        assertThat(notifier.running).isFalse();
    }

    @Test
    public void message_invokesWebhook() throws Exception {
        // Given a running notifier
        notifier.onApplicationEvent(mock(ApplicationReadyEvent.class));
        // ... and a webhook that responds
        webhook.enqueue(new MockResponse());

        // When a mapped metric is sent in json to the kafka topic
        String json = appConfig.objectMapper().writeValueAsString(newMappedMetricData());
        kafka.helper().produceStrings(appConfig.getKafkaTopic(), json);

        // Then, the notifier POSTs the json from the message into the webhook
        RecordedRequest webhookRequest = webhook.takeRequest();
        assertThat(webhookRequest.getMethod())
                .isEqualTo("POST");
        assertThat(webhookRequest.getBody().readUtf8())
                .isEqualTo(json);
    }

    static MappedMetricData newMappedMetricData() {
        TagCollection tags = new TagCollection(ImmutableMap.of(MTYPE, "gauge", UNIT, "metric"));
        MetricDefinition def = new MetricDefinition("latency", tags, TagCollection.EMPTY);
        MetricData data = new MetricData(def, 2.0f, Instant.now().getEpochSecond());
        UUID detectorUUID = UUID.randomUUID();
        MappedMetricData mappedData = new MappedMetricData(data, detectorUUID, "aquila-detector");
        mappedData.setAnomalyResult(new AnomalyResult(detectorUUID, data, AnomalyLevel.NORMAL));
        return mappedData;
    }

    static String bootstrapServers() {
        return kafka.helper()
                .consumerConfig()
                .getProperty(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG);
    }
}
