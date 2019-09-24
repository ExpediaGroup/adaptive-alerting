package com.expedia.adaptivealerting.kafka;

import com.expedia.adaptivealerting.kafka.util.ConfigUtil;
import com.expedia.metrics.MetricData;
import com.typesafe.config.Config;
import lombok.val;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;

public class KafkaMetricFunctions {

    private static final String AGGREGATOR_PRODUCER = "aggregator-producer";

    private Producer<String, MetricData> aggregatorProducer;

    public Producer<String, MetricData> getMetricFunctionsSink(Config config){
        val aggregatorProducerConfig = config.getConfig(AGGREGATOR_PRODUCER);
        val aggregatorProducerProps = ConfigUtil.toProducerConfig(aggregatorProducerConfig);
        aggregatorProducer = new KafkaProducer<>(aggregatorProducerProps);
        return aggregatorProducer;
    }
}
