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
package com.expedia.adaptivealerting.kafka;

import com.codahale.metrics.JmxReporter;
import com.expedia.adaptivealerting.core.util.ConfigUtil;
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

import static com.expedia.adaptivealerting.core.util.AssertUtil.notNull;
import static com.expedia.adaptivealerting.kafka.KafkaConfigProps.HEALTH_STATUS_PATH;
import static com.expedia.adaptivealerting.kafka.KafkaConfigProps.INBOUND_TOPIC;
import static com.expedia.adaptivealerting.kafka.KafkaConfigProps.STREAMS;

/**
 * Abstract base class for creating Kafka apps.
 *
 * @author David Sutherland
 * @author Willie Wheeler
 */
public abstract class AbstractKafkaApp {
    private final Config appConfig;
    private final Application application;
    
    public AbstractKafkaApp(Config appConfig) {
        notNull(appConfig, "appConfig can't be null");
        this.appConfig = appConfig;
        this.application = application();
    }
    
    public Config getAppConfig() {
        return appConfig;
    }
    
    public void start() {
        application.start();
    }
    
    /**
     * Implementations must provide an application topology using this method.
     *
     * @return A topology builder.
     */
    protected abstract StreamsBuilder streamsBuilder();
    
    private Application application() {
        final Application app = new Application(streamsRunner(), jmxReporter());
        Runtime.getRuntime().addShutdownHook(new Thread(app::stop));
        return app;
    }
    
    private StreamsRunner streamsRunner() {
        return new StreamsRunner(streamsFactory(), healthListener());
    }
    
    private StreamsFactory streamsFactory() {
        final StreamsBuilder builder = streamsBuilder();
        final Properties props = ConfigUtil.toProperties(appConfig.getConfig(STREAMS));
        final StreamsConfig streamsConfig = new StreamsConfig(props);
        final String inboundTopic = appConfig.getString(INBOUND_TOPIC);
        return new StreamsFactory(builder::build, streamsConfig, inboundTopic);
    }
    
    private StateChangeListener healthListener() {
        final HealthStatusController controller = new HealthStatusController();
        final String path = appConfig.getString(HEALTH_STATUS_PATH);
        controller.addListener(new UpdateHealthStatusFile(path));
        return new StateChangeListener(controller);
    }
    
    private JmxReporter jmxReporter() {
        return JmxReporter.forRegistry(MetricsRegistries.metricRegistry()).build();
    }
}
