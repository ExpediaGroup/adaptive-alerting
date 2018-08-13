package com.expedia.adaptivealerting.kafka.sync;

import com.expedia.adaptivealerting.core.data.Mpoint;
import com.expedia.adaptivealerting.core.util.ConfigUtil;
import com.expedia.adaptivealerting.core.util.ReflectionUtil;
import com.expedia.adaptivealerting.dataservice.DataSinkService;
import com.expedia.adaptivealerting.kafka.util.AppUtil;
import com.typesafe.config.Config;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static com.expedia.adaptivealerting.kafka.KafkaConfigProps.STREAMS;

public class KafkaDataSink {

    private static final String KAFKA_DATA_SINK = "kafka-data-sink";
    private boolean stop;

    public static void main(String[] s) {
        final Config appConfig = AppUtil.getAppConfig(KAFKA_DATA_SINK);
        final Properties props = ConfigUtil.toProperties(appConfig.getConfig(STREAMS));
        KafkaConsumer<String, Mpoint> kafkaConsumer = new KafkaConsumer<>(props);
        KafkaDataSink kafkaDataSink = new KafkaDataSink();
        kafkaDataSink.processKafkaRecords(kafkaConsumer, appConfig);
        Runtime.getRuntime().addShutdownHook(new Thread(kafkaDataSink::stop));
    }

    private void processKafkaRecords(KafkaConsumer<String, Mpoint> kafkaConsumer, Config appConfig) {
        while (!stop) {
            ConsumerRecords<String, Mpoint> consumerRecords = kafkaConsumer.poll(10000);
            List<Mpoint> sinkRecords = new ArrayList<>();
            consumerRecords.forEach(consumerRecord -> {
                if (shouldProcess(consumerRecord)) {
                    sinkRecords.add(consumerRecord.value());
                }
            });
            DataSinkService dataSinkService = (DataSinkService) ReflectionUtil.newInstance(appConfig.getString("data-sink-service"));
            dataSinkService.put(sinkRecords);
        }
    }

    private boolean shouldProcess(ConsumerRecord<String,Mpoint> consumerRecord) {
        String what = consumerRecord.value().getMetric().getTag("what");
        return what.equalsIgnoreCase("booking") || what.equalsIgnoreCase("eps-booking");
    }

    public void stop() {
        this.stop = true;
    }
}
