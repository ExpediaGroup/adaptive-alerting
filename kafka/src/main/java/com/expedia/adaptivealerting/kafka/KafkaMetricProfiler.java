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

import com.expedia.adaptivealerting.anomdetect.util.HttpClientWrapper;
import com.expedia.adaptivealerting.kafka.processor.MetricProfilerTransformerSupplier;
import com.expedia.adaptivealerting.kafka.serde.MetricDataJsonSerde;
import com.expedia.adaptivealerting.metricprofiler.MetricProfiler;
import com.expedia.adaptivealerting.metricprofiler.source.DefaultProfileSource;
import com.expedia.metrics.MetricData;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.typesafe.config.Config;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.StoreBuilder;
import org.apache.kafka.streams.state.Stores;

import static com.expedia.adaptivealerting.anomdetect.util.AssertUtil.notNull;

@Slf4j
public final class KafkaMetricProfiler extends AbstractStreamsApp {
    private static final String CK_METRIC_PROFILER = "metric-profiler";
    private static final String stateStoreName = "profiler-request-buffer";
    private static final String CK_MODEL_SERVICE_URI_TEMPLATE = "model-service-base-uri";

    private Serde<String> outputKeySerde = new Serdes.StringSerde();
    private Serde<MetricData> outputValueSerde = new MetricDataJsonSerde();

    @Getter
    private final MetricProfiler metricProfiler;

    public static void main(String[] args) {
        val tsConfig = new TypesafeConfigLoader(CK_METRIC_PROFILER).loadMergedConfig();
        val saConfig = new StreamsAppConfig(tsConfig);
        val metricProfiler = buildMetricProfiler(tsConfig);
        new KafkaMetricProfiler(saConfig, metricProfiler).start();
    }

    /**
     * Creates a new Kafka Streams adapter to send metrics to be profiled to profiling Kafka topic.
     *
     * @param config Streams app configuration.
     */
    public KafkaMetricProfiler(StreamsAppConfig config, MetricProfiler metricProfiler) {
        super(config);
        notNull(metricProfiler, "mapper can't be null");
        this.metricProfiler = metricProfiler;
    }

    @Override
    protected Topology buildTopology() {
        val config = getConfig();
        val inboundTopic = config.getInputTopic();
        val outboundTopic = config.getOutputTopic();

        log.info("Initializing: inboundTopic={}, outboundTopic={}", inboundTopic, outboundTopic);

        val builder = new StreamsBuilder();
        // create store
        StoreBuilder<KeyValueStore<String, MetricData>> keyValueStoreBuilder =
                Stores.keyValueStoreBuilder(Stores.inMemoryKeyValueStore(stateStoreName),
                        Serdes.String(),
                        new MetricDataJsonSerde())
                        .withLoggingDisabled();
        // register store
        builder.addStateStore(keyValueStoreBuilder);

        final KStream<String, MetricData> stream = builder.stream(inboundTopic);
        stream
                .filter((key, md) -> md != null)
                .transform(new MetricProfilerTransformerSupplier(metricProfiler, stateStoreName), stateStoreName)
                .to(outboundTopic, Produced.with(outputKeySerde, outputValueSerde));
        return builder.build();
    }

    static MetricProfiler buildMetricProfiler(Config config) {
        val uriTemplate = config.getString(CK_MODEL_SERVICE_URI_TEMPLATE);
        val profilingSource = new DefaultProfileSource(new HttpClientWrapper(), new ObjectMapper(), uriTemplate);
        return new MetricProfiler(profilingSource);
    }
}
