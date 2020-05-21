package com.expedia.adaptivealerting.kafka.detectorrunner;

import com.expedia.adaptivealerting.anomdetect.detect.MappedMetricData;
import com.expedia.adaptivealerting.kafka.TypesafeConfigLoader;
import com.expedia.adaptivealerting.kafka.util.ConfigUtil;
import com.typesafe.config.Config;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.springframework.stereotype.Component;

import java.util.Properties;

@Component
public class AnomalyProducer {

    private static final String ANOMALY_PRODUCER = "anomaly-producer";
    private static String APP = "detector-runner";

    private KafkaProducer<String, MappedMetricData> producer;

    private Config anomalyProducerConfig;

    public AnomalyProducer() {
        Config config = new TypesafeConfigLoader(APP).loadMergedConfig();
        anomalyProducerConfig = config.getConfig(ANOMALY_PRODUCER);
        Properties anomalyProducerProps = ConfigUtil.toProducerConfig(anomalyProducerConfig);
        producer = new KafkaProducer<>(anomalyProducerProps);
    }

    public KafkaProducer<String, MappedMetricData> getProducer() {
        return producer;
    }

    public Config getAnomalyProducerConfig() {
        return anomalyProducerConfig;
    }
}
