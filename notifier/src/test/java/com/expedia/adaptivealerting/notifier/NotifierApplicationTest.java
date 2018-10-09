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
package com.expedia.adaptivealerting.notifier;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.charithe.kafka.EphemeralKafkaBroker;
import com.github.charithe.kafka.KafkaJunitRule;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.TestPropertySourceUtils;

import static com.expedia.adaptivealerting.notifier.TestHelper.bootstrapServers;
import static com.expedia.adaptivealerting.notifier.TestHelper.newMappedMetricData;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(
    classes = {
        NotifierApplication.class,
        NotifierApplicationTest.ServiceDependencies.class
    },
    webEnvironment = SpringBootTest.WebEnvironment.MOCK,
    properties = { // Set the only required properties
        "kafka.consumer.bootstrap.servers=${bootstrap.servers}",
        "webhook.url=http://localhost:${webhook.port}/hook"
    }
)
@ContextConfiguration(initializers = {
    NotifierApplicationTest.ServiceDependencies.class
})
public class NotifierApplicationTest {

    @ClassRule public static KafkaJunitRule kafka = new KafkaJunitRule(EphemeralKafkaBroker.create());
    @ClassRule public static MockWebServer webhook = new MockWebServer();

    @Autowired ObjectMapper objectMapper;

    @Test
    public void message_invokesWebhook() throws Exception {
        // Given a webhook that responds
        webhook.enqueue(new MockResponse());

        // When a mapped metric is sent in json to the alerts topic
        String json = objectMapper.writeValueAsString(newMappedMetricData());
        kafka.helper().produceStrings("alerts", json);

        // Then, the notifier POSTs the json from the message into the webhook
        RecordedRequest webhookRequest = webhook.takeRequest();
        assertThat(webhookRequest.getMethod())
            .isEqualTo("POST");
        assertThat(webhookRequest.getBody().readUtf8())
            .isEqualTo(json);
    }

    /** This grabs ports from the junit rules and inlines properties accordingly */
    static class ServiceDependencies
        implements ApplicationContextInitializer<ConfigurableApplicationContext> {

        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            TestPropertySourceUtils.addInlinedPropertiesToEnvironment(applicationContext,
                "bootstrap.servers=" + bootstrapServers(kafka),
                "webhook.port=" + webhook.getPort()
            );
        }
    }
}
