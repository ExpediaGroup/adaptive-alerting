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

import com.expedia.adaptivealerting.anomdetect.util.AnomalyToMetricTransformer;
import com.expedia.adaptivealerting.core.data.MappedMetricData;
import com.expedia.adaptivealerting.kafka.serde.MetricDataSerde;
import com.expedia.metrics.metrictank.MetricTankIdFactory;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;

/**
 * Kafka Streams adapter for {@link com.expedia.adaptivealerting.anomdetect.util.AnomalyToMetricTransformer}.
 *
 * @author Willie Wheeler
 */
@Slf4j
public final class KafkaAnomalyToMetricTransformer extends AbstractStreamsApp {
    private static final String CK_ANOMALY_TO_METRIC_TRANSFORMER = "anomaly-to-metric-transformer";
    
    private final AnomalyToMetricTransformer transformer = new AnomalyToMetricTransformer();
    private final MetricTankIdFactory metricTankIdFactory = new MetricTankIdFactory();
    
    public static void main(String[] args) {
        val tsConfig = new TypesafeConfigLoader(CK_ANOMALY_TO_METRIC_TRANSFORMER).loadMergedConfig();
        val saConfig = new StreamsAppConfig(tsConfig);
        new KafkaAnomalyToMetricTransformer(saConfig).start();
    }
    
    /**
     * Creates a new Kafka Streams adapter for the {@link AnomalyToMetricTransformer}.
     *
     * @param config Streams app configuration.
     */
    public KafkaAnomalyToMetricTransformer(StreamsAppConfig config) {
        super(config);
    }
    
    @Override
    protected Topology buildTopology() {
        val config = getConfig();
        val inboundTopic = config.getInboundTopic();
        val outboundTopic = config.getOutboundTopic();
        
        log.info("Initializing: inboundTopic={}, outboundTopic={}", inboundTopic, outboundTopic);
        
        val builder = new StreamsBuilder();
        final KStream<String, MappedMetricData> stream = builder.stream(inboundTopic);
        stream
                .map((key, mappedMetricData) -> {
                    val anomalyResult = mappedMetricData.getAnomalyResult();
                    val metricData = transformer.transform(anomalyResult);
                    val metricDef = metricData.getMetricDefinition();
                    val metricHash = metricTankIdFactory.getId(metricDef);
                    return KeyValue.pair(metricHash, metricData);
                })
                // TODO Make outbound serde configurable. [WLW]
                .to(outboundTopic, Produced.with(new Serdes.StringSerde(), new MetricDataSerde()));
        
        return builder.build();
    }
}
