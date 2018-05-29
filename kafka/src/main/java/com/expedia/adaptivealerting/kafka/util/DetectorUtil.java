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

import com.expedia.adaptivealerting.core.OutlierDetector;
import com.expedia.adaptivealerting.core.OutlierLevel;
import com.expedia.www.haystack.commons.entities.MetricPoint;
import com.expedia.www.haystack.commons.entities.MetricType;
import com.expedia.www.haystack.commons.entities.encoders.Encoder;
import com.expedia.www.haystack.commons.entities.encoders.PeriodReplacementEncoder;
import com.expedia.www.haystack.commons.kstreams.serde.metricpoint.MetricTankSerde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.KStream;
import scala.collection.immutable.Map$;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.function.Function;

public class DetectorUtil {
    static final String OUTLIER_LEVEL_TAG = "outlierLevel";

    // FIXME Hack because I'm not sure how to handle null MetricPoints below.
    // But definitely don't want to keep this as it messes up the model.
    static final MetricPoint NULL_METRIC_POINT =
            new MetricPoint("null", MetricType.Gauge(), Map$.MODULE$.<String, String>empty(), 0, 0);
    private final static Encoder ENCODER = new PeriodReplacementEncoder();

    public static void startStreams(Function<String, OutlierDetector> detectorFactory, String appId, String topicName) {
        final Properties conf = new Properties();

        // TODO Move to config file.
        conf.put(StreamsConfig.APPLICATION_ID_CONFIG, appId);
        conf.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        conf.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        conf.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, MetricTankSerde.class);

        final StreamsBuilder builder = new StreamsBuilder();
        final KStream<String, MetricPoint> metrics = builder.stream(topicName);

        final Map<String, OutlierDetector> detectors = new HashMap<>();
        metrics
                .map((key, metricPoint) -> {
                    MetricPoint classified = evaluateMetric(metricPoint, detectors, detectorFactory);
                    return KeyValue.pair(null, classified);
                })
                .to("anomalies");

        final Topology topology = builder.build();

        final KafkaStreams streams = new KafkaStreams(topology, conf);

        streams.start();
        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
    }

    static MetricPoint evaluateMetric(
            MetricPoint metricPoint,
            Map<String, OutlierDetector> detectors,
            Function<String, OutlierDetector> detectorFactory
    ) {
        // FIXME Hack, see above
        if (metricPoint == null) {
            metricPoint = NULL_METRIC_POINT;
        }
        final String metricId = extractMetricId(metricPoint);
        if (!detectors.containsKey(metricId)) {
            detectors.put(metricId, detectorFactory.apply(metricId));
        }

        final OutlierDetector detector = detectors.get(metricId);
        final Instant instant = Instant.ofEpochSecond(metricPoint.epochTimeInSeconds());
        final OutlierLevel level = detector.evaluate(instant, metricPoint.value());
        return classifiedMetricPoint(metricPoint, level);
    }

    static String extractMetricId(MetricPoint metricPoint) {
        return metricPoint.getMetricPointKey(ENCODER); // TODO: find out how we'll be getting the ids.
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
