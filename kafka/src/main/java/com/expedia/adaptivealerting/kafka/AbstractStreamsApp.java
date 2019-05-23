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
package com.expedia.adaptivealerting.kafka;

import com.codahale.metrics.JmxReporter;
import com.codahale.metrics.MetricRegistry;
import lombok.Getter;
import lombok.val;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.Topology;

import static com.expedia.adaptivealerting.core.util.AssertUtil.notNull;

/**
 * Abstract base class for Kafka Streams apps. See
 * <p>
 * https://kafka.apache.org/10/documentation/streams/developer-guide/write-streams
 * <p>
 * for more information on writing streams apps.
 */
public abstract class AbstractStreamsApp {

    @Getter
    private final StreamsAppConfig config;

    @Getter
    private final JmxReporter jmxReporter;

    public AbstractStreamsApp(StreamsAppConfig config) {
        notNull(config, "config can't be null");
        this.config = config;
        this.jmxReporter = JmxReporter.forRegistry(new MetricRegistry())
                .build();

    }

    public AbstractStreamsApp(StreamsAppConfig config, JmxReporter reporter) {
        notNull(config, "config can't be null");
        notNull(reporter, "reporter can't be null");
        this.config = config;
        this.jmxReporter = reporter;
    }

    public void start() {
        val streams = new KafkaStreams(buildTopology(), config.getStreamsConfig());
        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
        jmxReporter.start();
        streams.start();
    }

    protected abstract Topology buildTopology();
}
