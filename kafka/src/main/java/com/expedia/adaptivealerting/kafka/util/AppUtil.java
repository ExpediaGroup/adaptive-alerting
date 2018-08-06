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
import com.expedia.www.haystack.commons.kstreams.app.Application;
import com.expedia.www.haystack.commons.kstreams.app.StreamsRunner;
import com.expedia.www.haystack.commons.metrics.MetricsRegistries;
import com.typesafe.config.Config;

public final class AppUtil {
    
    /**
     * Prevent instantiation.
     */
    private AppUtil() {
    }

    public static Config getAppConfig(String configKey) {
        // TODO Remove Haystack-specific references. [WLW]
        Config config = ConfigurationLoader.loadConfigFileWithEnvOverrides("config/base.conf", "HAYSTACK_PROP_");
        return config.getConfig(configKey).withFallback(config.getConfig("kstream.app.default"));
    }
    
    /**
     * @param streamsRunner Streams runner.
     * @deprecated Deprecated in favor of {@link com.expedia.adaptivealerting.kafka.AbstractKafkaApp}.
     */
    @Deprecated
    public static void launchStreamRunner(StreamsRunner streamsRunner) {
        JmxReporter jmxReporter = JmxReporter.forRegistry(MetricsRegistries.metricRegistry()).build();
        Application app = new Application(streamsRunner, jmxReporter);
        Runtime.getRuntime().addShutdownHook(new Thread(app::stop));
        app.start();
    }
}
