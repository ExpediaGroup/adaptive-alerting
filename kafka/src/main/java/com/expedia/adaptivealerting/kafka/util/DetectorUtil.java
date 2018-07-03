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

import com.expedia.adaptivealerting.anomdetect.AnomalyDetector;
import com.expedia.adaptivealerting.core.anomaly.AnomalyResult;
import com.expedia.adaptivealerting.kafka.detector.KafkaAnomalyDetectorManager;
import com.expedia.adaptivealerting.kafka.serde.JsonPojoSerializer;
import com.expedia.adaptivealerting.kafka.serde.JsonPojoDeserializer;
import com.expedia.www.haystack.commons.entities.MetricPoint;
import com.expedia.www.haystack.commons.entities.MetricType;
import com.expedia.www.haystack.commons.entities.encoders.Encoder;
import com.expedia.www.haystack.commons.entities.encoders.PeriodReplacementEncoder;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;
import scala.collection.immutable.Map$;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * @deprecated Deprecated in favor of {@link KafkaAnomalyDetectorManager}.
 */
@Deprecated
public class DetectorUtil {

    // FIXME Hack because I'm not sure how to handle null MetricPoints below.
    // But definitely don't want to keep this as it messes up the model.
    static final MetricPoint NULL_METRIC_POINT =
            new MetricPoint("null", MetricType.Gauge(), Map$.MODULE$.<String, String>empty(), 0, 0);
    private final static Encoder ENCODER = new PeriodReplacementEncoder();

    public static StreamsBuilder createDetectorStreamsBuilder(
            String topicName, Function<String, AnomalyDetector> detectorFactory) {
        final StreamsBuilder builder = new StreamsBuilder();
        final KStream<String, MetricPoint> metrics = builder.stream(topicName);

        final Map<String, AnomalyDetector> detectors = new HashMap<>();
        metrics
                .map((key, metricPoint) -> {
                    AnomalyResult classified = classify(metricPoint, detectors, detectorFactory);
                    return KeyValue.pair(key, classified);
                })
                .to("anomalies", Produced.valueSerde(
                        Serdes.serdeFrom(new JsonPojoSerializer<>(), new JsonPojoDeserializer<>())));
        return builder;
    }

    static AnomalyResult classify(
            MetricPoint metricPoint,
            Map<String, AnomalyDetector> detectors,
            Function<String, AnomalyDetector> detectorFactory
    ) {
        // FIXME Hack, see above
        if (metricPoint == null) {
            metricPoint = NULL_METRIC_POINT;
        }
        final String metricId = extractMetricId(metricPoint);
        if (!detectors.containsKey(metricId)) {
            detectors.put(metricId, detectorFactory.apply(metricId));
        }

        final AnomalyDetector detector = detectors.get(metricId);
        return detector.classify(metricPoint);
    }

    static String extractMetricId(MetricPoint metricPoint) {
        // TODO: find out how we'll be getting the ids.
        return metricPoint.getMetricPointKey(ENCODER);
    }
}
