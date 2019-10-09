package com.expedia.adaptivealerting.kafka;

import com.expedia.adaptivealerting.kafka.util.ConfigUtil;
import com.expedia.adaptivealerting.metrics.functions.MetricFunctionsTask;
import com.expedia.adaptivealerting.metrics.functions.sink.MetricFunctionsPublish;
import com.expedia.adaptivealerting.metrics.functions.source.MetricFunctionsReader;
import com.expedia.adaptivealerting.metrics.functions.source.MetricFunctionsSpec;
import com.expedia.metrics.MetricData;
import com.typesafe.config.Config;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
public class KafkaMetricFunctions implements MetricFunctionsPublish {

    private static final String APP_ID = "aa-metric-functions";
    private static final String AGGREGATOR_PRODUCER = "aggregator-producer";
    private static final String METRIC_SOURCE_SINK = "metric-source-sink";
    private final String OUTPUT_TOPIC_KEY_STRING = "output-topic";
    private static final String INPUT_FUNCTIONS_FILENAME ="functions.txt";
    private final static String INPUT_FILE_PATH = "/config/";
    private static final int corePoolSize = 5;

    private Producer<String, MetricData> aggregatorProducer;

    private Config metricSinkConfig;

    public void initPublisher() {
        val config = new TypesafeConfigLoader(APP_ID).loadMergedConfig();
        val aggregatorProducerConfig = config.getConfig(AGGREGATOR_PRODUCER);
        metricSinkConfig = config.getConfig(METRIC_SOURCE_SINK);
        val aggregatorProducerProps = ConfigUtil.toProducerConfig(aggregatorProducerConfig);
        aggregatorProducer = new KafkaProducer<>(aggregatorProducerProps);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void publishMetrics(MetricData metricData) {
        try {
            ProducerRecord aggregateProducerRecord = new ProducerRecord(
                    metricSinkConfig.getString(OUTPUT_TOPIC_KEY_STRING),
                    metricData.getMetricDefinition().getKey(), metricData);
            aggregatorProducer.send(aggregateProducerRecord);
            log.info("Record sent for function: {}", metricData.getMetricDefinition().getKey());
        } catch (Exception e) {
            log.error("Exception while sending to kafka", e);
        }
    }

    public static void main(String[] args) {
        val config = new TypesafeConfigLoader(APP_ID).loadMergedConfig();
        val metricSourceConfig = config.getConfig(METRIC_SOURCE_SINK);
        // This is absolute path of the file at run time environment
        val input_file = INPUT_FILE_PATH + INPUT_FUNCTIONS_FILENAME;
        List<MetricFunctionsSpec> metricFunctionSpecs = MetricFunctionsReader.readFromInputFile(input_file);
        if (metricFunctionSpecs.isEmpty()) {
            log.error("Error with input functions file, exiting..." );
        }
        KafkaMetricFunctions metricFunctionsPublish = new KafkaMetricFunctions();
        metricFunctionsPublish.initPublisher();
        ScheduledExecutorService execService
                = Executors.newScheduledThreadPool(corePoolSize);
        for (MetricFunctionsSpec metricFunctionSpec: metricFunctionSpecs) {
            MetricFunctionsTask metricFunctionsTask = new MetricFunctionsTask(metricFunctionSpec,
                    metricFunctionsPublish,
                    metricSourceConfig);
            execService.scheduleAtFixedRate(metricFunctionsTask,
                    0, metricFunctionSpec.getIntervalInSecs(), TimeUnit.SECONDS);
        }

    }
}
