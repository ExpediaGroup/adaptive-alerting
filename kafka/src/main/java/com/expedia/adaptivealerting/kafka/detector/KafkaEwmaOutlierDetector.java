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
package com.expedia.adaptivealerting.kafka.detector;

import com.expedia.adaptivealerting.core.OutlierLevel;
import com.expedia.adaptivealerting.core.detector.EwmaOutlierDetector;
import com.expedia.www.haystack.commons.entities.MetricPoint;
import com.expedia.www.haystack.commons.kstreams.serde.metricpoint.MetricTankSerde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.KStream;

import java.time.Instant;
import java.util.Properties;

/**
 * Kafka Streams application for the EWMA outlier detector.
 *
 * @author Willie Wheeler
 */
public class KafkaEwmaOutlierDetector {
    private static final String OUTLIER_LEVEL_TAG = "outlierLevel";
    
    public static void main(String[] args) {
        
        // FIXME Create a map of these, rather than just using a single one across the board.
        // This will be a little involved because currently we are using metric names like "latency", and there are
        // many distinct series with that name. So we have to decide whether we want names to be unique or whether we
        // want to use tags for unique names.
        // But fine for now as this is just a demo. [WLW]
        final EwmaOutlierDetector detector = new EwmaOutlierDetector(0.8, 2.0, 3.0, 100.0);
        
        // TODO Move to config file.
        final Properties conf = new Properties();
        conf.put(StreamsConfig.APPLICATION_ID_CONFIG, "ewma-outlier-detector");
        conf.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        conf.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        conf.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, MetricTankSerde.class);
        
        final StreamsBuilder builder = new StreamsBuilder();
        final KStream<String, MetricPoint> metrics = builder.stream("metrics");
        
        metrics
                .map((key, metricPoint) -> {
                    if (metricPoint != null) {
                        final Instant instant = Instant.ofEpochSecond(metricPoint.epochTimeInSeconds());
                        final OutlierLevel level = detector.evaluate(instant, metricPoint.value());
                        final MetricPoint classified = classifiedMetricPoint(metricPoint, level);
                        return KeyValue.pair(null, classified);
                    } else {
                        return null;
                    }
                })
                .filterNot((key, metricPoint) ->
                        OutlierLevel.NORMAL.name().equals(metricPoint.tags().get(OUTLIER_LEVEL_TAG).get()))
                .to("anomalies");
        
        final Topology topology = builder.build();
        final KafkaStreams streams = new KafkaStreams(topology, conf);
        
        streams.start();
    }
    
    private static MetricPoint classifiedMetricPoint(MetricPoint metricPoint, OutlierLevel level) {
        return new MetricPoint(
                metricPoint.metric(),
                metricPoint.type(),
                metricPoint.tags().updated(OUTLIER_LEVEL_TAG, level.name()),
                metricPoint.value(),
                metricPoint.epochTimeInSeconds());
    }
}
