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

import com.codahale.metrics.JmxReporter;
import com.expedia.www.haystack.commons.config.ConfigurationLoader;
import com.expedia.www.haystack.commons.health.HealthStatusController;
import com.expedia.www.haystack.commons.health.UpdateHealthStatusFile;
import com.expedia.www.haystack.commons.kstreams.app.Application;
import com.expedia.www.haystack.commons.kstreams.app.StateChangeListener;
import com.expedia.www.haystack.commons.kstreams.app.StreamsFactory;
import com.expedia.www.haystack.commons.kstreams.app.StreamsRunner;
import com.expedia.www.haystack.commons.metrics.MetricsRegistries;
import com.typesafe.config.Config;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;

import java.util.Properties;

public class AppUtil {
    /**
     * Prevent instantiation.
     */
    private AppUtil() {
    }

    public static Config getAppConfig(String configKey) {
        Config config = ConfigurationLoader.loadConfigFileWithEnvOverrides("config/base.conf", "HAYSTACK_PROP_");
        return config.getConfig(configKey).withFallback(config.getConfig("kstream.app.default"));
    }

    public static void launchStreamRunner(StreamsRunner streamsRunner) {
        //create an instance of the application
        JmxReporter jmxReporter = JmxReporter.forRegistry(MetricsRegistries.metricRegistry()).build();
        Application app = new Application(streamsRunner, jmxReporter);

        //start the application
        app.start();

        //add a shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(app::stop));
    }

    public static StreamsRunner createStreamsRunner(Config appConfig, StreamsBuilder builder) {
        String healthStatusPath = appConfig.getString("health.status.path");
        String topic = appConfig.getString("topic");

        HealthStatusController healthStatusController = new HealthStatusController();
        healthStatusController.addListener(new UpdateHealthStatusFile(healthStatusPath));

        StateChangeListener stateChangeListener = new StateChangeListener(healthStatusController);


        StreamsConfig streamsConfig = new StreamsConfig(configToProps(appConfig.getConfig("streams")));


        StreamsFactory streamsFactory = new StreamsFactory(builder::build, streamsConfig, topic);

        return new StreamsRunner(streamsFactory, stateChangeListener);
    }

    private static Properties configToProps(Config config) {
        Properties props = new Properties();
        config.entrySet().forEach((entry) -> {
            props.setProperty(entry.getKey(), entry.getValue().unwrapped().toString());
        });
        return props;
    }
}
