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

import com.expedia.adaptivealerting.anomdetect.DetectorMapper;
import com.expedia.adaptivealerting.anomdetect.mapper.MapperResult;
import com.expedia.adaptivealerting.core.data.MappedMetricData;
import com.expedia.adaptivealerting.kafka.processor.MetricDataTransformerSupplier;
import com.expedia.adaptivealerting.kafka.serde.MappedMetricDataJsonSerde;
import com.expedia.adaptivealerting.kafka.serde.MetricDataJsonSerde;
import com.expedia.adaptivealerting.kafka.util.DetectorUtil;
import com.expedia.metrics.MetricData;
import lombok.Generated;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.StoreBuilder;
import org.apache.kafka.streams.state.Stores;

import java.util.stream.Collectors;

import static com.expedia.adaptivealerting.core.util.AssertUtil.notNull;

/**
 * Kafka Streams adapter for {@link DetectorMapper}. Reads metric data from an input topic, classifies individual metric
 * data points, and publishes the classifications to an anomalies output topic.
 */
@Slf4j
public final class KafkaAnomalyDetectorMapper extends AbstractStreamsApp {
    private static final String CK_AD_MAPPER = "ad-mapper";
    private static final String stateStoreName = "es-request-buffer";

    private final DetectorMapper mapper;

    // TODO Make these configurable. [WLW]
    private Serde<String> outputKeySerde = new Serdes.StringSerde();
    private Serde<MappedMetricData> outputValueSerde = new MappedMetricDataJsonSerde();

    // Cleaned code coverage
    // https://reflectoring.io/100-percent-test-coverage/
    @Generated
    public static void main(String[] args) {
        val config = new TypesafeConfigLoader(CK_AD_MAPPER).loadMergedConfig();
        val saConfig = new StreamsAppConfig(config);
        val detectorSource = DetectorUtil.buildDetectorSource(config);
        val mapper = new DetectorMapper(detectorSource);
        new KafkaAnomalyDetectorMapper(saConfig, mapper).start();
    }

    /**
     * Creates a new Kafka Streams adapter for the {@link DetectorMapper}.
     *
     * @param config Streams app configuration.
     * @param mapper Anomaly detector mapper.
     */
    public KafkaAnomalyDetectorMapper(StreamsAppConfig config, DetectorMapper mapper) {
        super(config);
        notNull(mapper, "mapper can't be null");
        this.mapper = mapper;
    }

    @Override
    protected Topology buildTopology() {
        val config = getConfig();
        val inputTopic = config.getInputTopic();
        val outputTopic = config.getOutputTopic();
        log.info("Initializing: inputTopic={}, outputTopic={}", inputTopic, outputTopic);

        val builder = new StreamsBuilder();

        // create store
        StoreBuilder<KeyValueStore<String, MetricData>> keyValueStoreBuilder =
                Stores.keyValueStoreBuilder(Stores.inMemoryKeyValueStore(stateStoreName),
                        Serdes.String(),
                        new MetricDataJsonSerde())
                        .withLoggingDisabled();
        // register store
        builder.addStateStore(keyValueStoreBuilder);

        final KStream<String, MetricData> stream = builder.stream(inputTopic);
        stream
                .filter((key, md) -> md != null)
                .transform(new MetricDataTransformerSupplier(mapper, stateStoreName), stateStoreName)
                .flatMap(this::metricsByDetector)
                .to(outputTopic, Produced.with(outputKeySerde, outputValueSerde));
        return builder.build();
    }


    private Iterable<? extends KeyValue<String, MappedMetricData>> metricsByDetector(String key, MapperResult mmRes) {
        assert mmRes != null;
        return mmRes.getMatchingDetectors().stream()
                .map(detector -> KeyValue.pair(detector.getUuid().toString(), new MappedMetricData(mmRes.getMetricData(), detector.getUuid())))
                .collect(Collectors.toSet());
    }
}
