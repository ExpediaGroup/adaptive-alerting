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

import com.expedia.adaptivealerting.core.anomaly.AnomalyLevel;
import com.expedia.adaptivealerting.core.data.MappedMetricData;
import com.expedia.adaptivealerting.kafka.serde.JsonPojoSerde;
import com.expedia.alertmanager.model.Alert;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;

import java.util.HashMap;

@Slf4j
public class KafkaAnomalyToAlertMapper extends AbstractStreamsApp {
    private static final String APP_ID = "a2a-mapper";

    private static final String VALUE = "value";
    private static final String KEY = "key";
    private static final String TIMESTAMP = "timestamp";
    private static final String ANOMALY_LEVEL = "anomalyLevel";


    public static void main(String[] args) {
        val tsConfig = new TypesafeConfigLoader(APP_ID).loadMergedConfig();
        val saConfig = new StreamsAppConfig(tsConfig);
        new KafkaAnomalyToAlertMapper(saConfig).start();
    }

    /**
     * Creates a new Kafka Streams adapter to map anomalies to alerts.
     *
     * @param config Streams app configuration.
     */
    public KafkaAnomalyToAlertMapper(StreamsAppConfig config) {
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
        stream.filter((key, mappedMetricData) -> AnomalyLevel.STRONG.equals(mappedMetricData.getAnomalyResult().getAnomalyLevel()) ||
                AnomalyLevel.WEAK.equals(mappedMetricData.getAnomalyResult().getAnomalyLevel()))
                .map((key, mappedMetricData) -> {
                    val alert = new Alert();
                    alert.setName(mappedMetricData.getMetricData().getMetricDefinition().getKey());
                    val tags = mappedMetricData.getMetricData().getMetricDefinition().getTags().getKv();
                    val labels = new HashMap<String, String>(tags);
                    labels.put(KEY, mappedMetricData.getMetricData().getMetricDefinition().getKey());
                    alert.setLabels(labels);
                    Double value = mappedMetricData.getMetricData().getValue();
                    Long timestamp = mappedMetricData.getMetricData().getTimestamp();
                    AnomalyLevel anomalyLevel = mappedMetricData.getAnomalyResult().getAnomalyLevel();
                    val annotations = new HashMap<String, String>() {{
                        put(VALUE, value.toString());
                        put(TIMESTAMP, timestamp.toString());
                        put(ANOMALY_LEVEL, anomalyLevel.toString());
                    }};
                    alert.setAnnotations(annotations);
                    return KeyValue.pair(mappedMetricData.getDetectorUuid().toString(), alert);
                })
                .to(outboundTopic, Produced.with(new Serdes.StringSerde(), new JsonPojoSerde<>()));

        return builder.build();
    }
}
