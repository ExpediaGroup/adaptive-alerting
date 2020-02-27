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

import com.github.charithe.kafka.EphemeralKafkaBroker;
import com.github.charithe.kafka.KafkaJunitRule;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.springframework.boot.autoconfigure.context.PropertyPlaceholderAutoConfiguration;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import static com.expedia.adaptivealerting.kafka.util.TestHelper.bootstrapServers;
import static com.expedia.adaptivealerting.kafka.util.TestHelper.newMappedMetricData;
import static org.mockito.Mockito.mock;
import static org.springframework.boot.test.util.EnvironmentTestUtils.addEnvironment;

public class NotifierTest {

    @ClassRule
    public static KafkaJunitRule kafka = new KafkaJunitRule(EphemeralKafkaBroker.create()).waitForStartup();

    @ClassRule
    public static MockWebServer webhook = new MockWebServer();

    AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();

    Notifier notifier;
    NotifierConfig notifierConfig;

    @Before
    public void init() {
        addEnvironment(context,
                "kafka.consumer.bootstrap.servers=" + bootstrapServers(kafka),
                "webhook.url=http://localhost:" + webhook.getPort() + "/hook"
        );

        context.register(PropertyPlaceholderAutoConfiguration.class, NotifierConfig.class, Notifier.class);
        context.refresh();
        notifier = context.getBean(Notifier.class);
        notifierConfig = context.getBean(NotifierConfig.class);
    }

    @After
    public void close() {
        context.close();
    }

    @Test
    public void applicationReadyEvent_startsNotifier() {
        Assertions.assertThat(notifier.running).isFalse();

        notifier.onApplicationEvent(mock(ApplicationReadyEvent.class));

        Assertions.assertThat(notifier.running).isTrue();
    }

    @Test
    public void context_close_stopsNotifier() {
        notifier.onApplicationEvent(mock(ApplicationReadyEvent.class));

        context.close();

        Assertions.assertThat(notifier.running).isFalse();
    }

    @Test
    public void message_invokesWebhook() throws Exception {
        // Given a running notifier
        notifier.onApplicationEvent(mock(ApplicationReadyEvent.class));
        // ... and a webhook that responds
        webhook.enqueue(new MockResponse());

        // When a mapped metric is sent in json to the kafka topic
        String json = notifierConfig.objectMapper().writeValueAsString(newMappedMetricData());
        kafka.helper().produceStrings(notifierConfig.getKafkaTopic(), json);

        // Then, the notifier POSTs the json from the message into the webhook
        RecordedRequest webhookRequest = webhook.takeRequest();
        Assertions.assertThat(webhookRequest.getMethod())
                .isEqualTo("POST");
        Assertions.assertThat(webhookRequest.getBody().readUtf8())
                .isEqualTo(json);
    }
}
