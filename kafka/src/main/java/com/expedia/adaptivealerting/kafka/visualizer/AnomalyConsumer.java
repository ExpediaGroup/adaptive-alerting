package com.expedia.adaptivealerting.kafka.visualizer;

import com.expedia.adaptivealerting.anomdetect.detect.MappedMetricData;
import com.expedia.adaptivealerting.kafka.TypesafeConfigLoader;
import com.expedia.adaptivealerting.kafka.util.ConfigUtil;
import com.typesafe.config.Config;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;

import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


@Slf4j
public class AnomalyConsumer {

    private KafkaConsumer<String, MappedMetricData> kafkaConsumer;
    private static String METRIC_TOPIC = "anomalies";
    private static long POLL_INTERVAL = 1000L;

    private AnomaliesProcessor anomaliesProcessor;
    private ExecutorService executorService;

    public AnomalyConsumer() {
        Config config = new TypesafeConfigLoader("visualizer").loadMergedConfig();
        Config metricConsumerConfig = config.getConfig("metric-consumer");
        Properties metricConsumerProps = ConfigUtil.toConsumerConfig(metricConsumerConfig);
        kafkaConsumer = new KafkaConsumer(metricConsumerProps);
        anomaliesProcessor = new AnomaliesProcessor();
        executorService = new ThreadPoolExecutor(10, 10, 0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>());
    }

    public void listen() {
        kafkaConsumer.subscribe(Collections.singletonList(METRIC_TOPIC));
        boolean continueProcessing = true;

        // See Kafka: The Definitive Guide, pp. 86 ff.
        while (continueProcessing) {
            try {
                ConsumerRecords<String, MappedMetricData> metricRecords = kafkaConsumer.poll(POLL_INTERVAL);
                int numConsumed = metricRecords.count();
                log.trace("Read {} metric records from topic={}", numConsumed, METRIC_TOPIC);
                anomaliesProcessor.processMetrics(metricRecords, executorService);
            } catch (WakeupException e) {
                kafkaConsumer.close();
                continueProcessing = false;
            } catch (Exception e) {
                log.error(e.getLocalizedMessage());
                e.printStackTrace();
            }
        }

    }
}
