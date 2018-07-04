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
package com.expedia.adaptivealerting.kafka.util;

import com.expedia.www.haystack.commons.health.HealthStatusController;
import com.expedia.www.haystack.commons.health.UpdateHealthStatusFile;
import com.expedia.www.haystack.commons.kstreams.app.StateChangeListener;
import com.expedia.www.haystack.commons.kstreams.app.StreamsFactory;
import com.expedia.www.haystack.commons.kstreams.app.StreamsRunner;
import com.typesafe.config.Config;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;

import java.util.Properties;

import static com.expedia.adaptivealerting.kafka.KafkaConfigProps.HEALTH_STATUS_PATH;
import static com.expedia.adaptivealerting.kafka.KafkaConfigProps.INBOUND_TOPIC;
import static com.expedia.adaptivealerting.kafka.KafkaConfigProps.STREAMS;

/**
 * @deprecated Deprecated in favor of {@link com.expedia.adaptivealerting.kafka.AbstractKafkaApp}.
 */
@Deprecated
public abstract class BaseStreamRunnerBuilder {
    public abstract StreamsRunner build(Config config);

    protected StreamsRunner createStreamsRunner(Config appConfig, StreamsBuilder builder) {
        String healthStatusPath = appConfig.getString(HEALTH_STATUS_PATH);
        String topic = appConfig.getString(INBOUND_TOPIC);

        HealthStatusController healthStatusController = new HealthStatusController();
        healthStatusController.addListener(new UpdateHealthStatusFile(healthStatusPath));

        StateChangeListener stateChangeListener = new StateChangeListener(healthStatusController);

        StreamsConfig streamsConfig = new StreamsConfig(configToProp(appConfig.getConfig(STREAMS)));

        StreamsFactory streamsFactory = new StreamsFactory(builder::build, streamsConfig, topic);

        return new StreamsRunner(streamsFactory, stateChangeListener);
    }

    private Properties configToProp(Config config) {
        Properties props = new Properties();
        config.entrySet().forEach((entry) -> {
            props.setProperty(entry.getKey(), entry.getValue().unwrapped().toString());
        });
        return props;
    }
}
