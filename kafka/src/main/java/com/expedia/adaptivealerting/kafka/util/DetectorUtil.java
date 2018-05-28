package com.expedia.adaptivealerting.kafka.util;

import com.expedia.adaptivealerting.core.OutlierDetector;
import com.expedia.adaptivealerting.core.OutlierLevel;
import com.expedia.www.haystack.commons.entities.MetricPoint;
import com.expedia.www.haystack.commons.entities.MetricType;
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
import java.util.Properties;

public class DetectorUtil {
    private static final String OUTLIER_LEVEL_TAG = "outlierLevel";

    // FIXME Hack because I'm not sure how to handle null MetricPoints below.
    // But definitely don't want to keep this as it messes up the model.
    private static final MetricPoint NULL_METRIC_POINT =
            new MetricPoint("null", MetricType.Gauge(), Map$.MODULE$.<String, String>empty(), 0, 0);

    public static void startStreams(OutlierDetector detector, String appId, String topicName) {
        final Properties conf = new Properties();

        // TODO Move to config file.
        conf.put(StreamsConfig.APPLICATION_ID_CONFIG, appId);
        conf.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        conf.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        conf.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, MetricTankSerde.class);

        final StreamsBuilder builder = new StreamsBuilder();
        final KStream<String, MetricPoint> metrics = builder.stream(topicName);

        metrics
                .map((key, metricPoint) -> {
                    // FIXME Hack, see above
                    if (metricPoint == null) {
                        metricPoint = NULL_METRIC_POINT;
                    }
                    final Instant instant = Instant.ofEpochSecond(metricPoint.epochTimeInSeconds());
                    final OutlierLevel level = detector.evaluate(instant, metricPoint.value());
                    MetricPoint classified = classifiedMetricPoint(metricPoint, level);
                    return KeyValue.pair(null, classified);
                })
                .to("anomalies");

        final Topology topology = builder.build();

        final KafkaStreams streams = new KafkaStreams(topology, conf);

        streams.start();
        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
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
