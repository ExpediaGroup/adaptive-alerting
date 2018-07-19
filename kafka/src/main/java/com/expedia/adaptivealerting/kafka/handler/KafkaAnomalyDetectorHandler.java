package com.expedia.adaptivealerting.kafka.handler;

import com.expedia.adaptivealerting.kafka.AbstractKafkaApp;
import com.expedia.adaptivealerting.kafka.util.AppUtil;
import com.typesafe.config.Config;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.KStream;

import static com.expedia.adaptivealerting.kafka.KafkaConfigProps.*;

public class KafkaAnomalyDetectorHandler extends AbstractKafkaApp {
    private final Double perfScore;

    private KafkaAnomalyDetectorHandler(Config appConfig, Double perfScore) {
        super(appConfig);
        this.perfScore = perfScore;
    }

    public static void sendToKafka(Double perfScore) {
        final Config appConfig = AppUtil.getAppConfig(PERFORMANCE_MONITOR);
        new KafkaAnomalyDetectorHandler(appConfig, perfScore).start();
    }

    @Override
    protected StreamsBuilder streamsBuilder() {
        final String inboundTopic = getAppConfig().getString(INBOUND_TOPIC);
        final String outboundTopic = getAppConfig().getString(OUTBOUND_TOPIC);
        final StreamsBuilder builder = new StreamsBuilder();
        final KStream<String, Double> stream = builder.stream(inboundTopic);
        stream
                .mapValues(Double -> perfScore)
                .filter((key, perfScore) -> perfScore != null)
                .to(outboundTopic);
        return builder;
    }
}
