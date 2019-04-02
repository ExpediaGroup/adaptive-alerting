/*
 * Copyright 2018-2019 Expedia Group, Inc.
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
package com.expedia.adaptivealerting.kafka;

import com.expedia.adaptivealerting.anomdetect.AnomalyToMetricMapper;
import com.expedia.adaptivealerting.core.anomaly.AnomalyLevel;
import com.expedia.adaptivealerting.core.anomaly.AnomalyResult;
import com.expedia.adaptivealerting.core.data.MappedMetricData;
import com.expedia.adaptivealerting.kafka.util.ConfigUtil;
import com.expedia.metrics.MetricData;
import com.expedia.metrics.MetricDefinition;
import com.expedia.metrics.metrictank.MetricTankIdFactory;
import com.typesafe.config.Config;
import lombok.Generated;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.errors.WakeupException;

import java.util.Collections;

/**
 * Maps anomalies to metrics. Note that the input topic actually contains {@link MappedMetricData} rather than
 * {@link AnomalyResult}.
 */
@Slf4j
public class KafkaAnomalyToMetricMapper implements Runnable {
    private static final String APP_ID = "a2m-mapper";
    private static final String ANOMALY_CONSUMER = "anomaly-consumer";
    private static final String METRIC_PRODUCER = "metric-producer";
    private static final String TOPIC = "topic";
    private static final long POLL_PERIOD = 1000L;

    private final AnomalyToMetricMapper mapper = new AnomalyToMetricMapper();

    // TODO Replace this with the non-MetricTank version. [WLW]
    private final MetricTankIdFactory metricTankIdFactory = new MetricTankIdFactory();

    @Getter
    private final Consumer<String, MappedMetricData> anomalyConsumer;

    @Getter
    private final Producer<String, MetricData> metricProducer;

    @Getter
    private String anomalyTopic;

    @Getter
    private String metricTopic;

    // Cleaned code coverage
    // https://reflectoring.io/100-percent-test-coverage/
    @Generated
    public static void main(String[] args) {
        // TODO Refactor the loader such that it's not tied to Kafka Streams. [WLW]
        buildMapper(new TypesafeConfigLoader(APP_ID).loadMergedConfig()).run();
    }

    // Extracted for unit testing
    static KafkaAnomalyToMetricMapper buildMapper(Config config) {
        val anomalyConsumerConfig = config.getConfig(ANOMALY_CONSUMER);
        val anomalyConsumerTopic = anomalyConsumerConfig.getString(TOPIC);
        val anomalyConsumerProps = ConfigUtil.toConsumerConfig(anomalyConsumerConfig);
        val anomalyConsumer = new KafkaConsumer<String, MappedMetricData>(anomalyConsumerProps);

        val metricProducerConfig = config.getConfig(METRIC_PRODUCER);
        val metricProducerTopic = metricProducerConfig.getString(TOPIC);
        val metricProducerProps = ConfigUtil.toProducerConfig(metricProducerConfig);
        val metricProducer = new KafkaProducer<String, MetricData>(metricProducerProps);

        return new KafkaAnomalyToMetricMapper(
                anomalyConsumer,
                metricProducer,
                anomalyConsumerTopic,
                metricProducerTopic);
    }

    public KafkaAnomalyToMetricMapper(
            Consumer<String, MappedMetricData> anomalyConsumer,
            Producer<String, MetricData> metricProducer,
            String anomalyTopic,
            String metricTopic) {

        this.anomalyConsumer = anomalyConsumer;
        this.metricProducer = metricProducer;
        this.anomalyTopic = anomalyTopic;
        this.metricTopic = metricTopic;
    }

    @Override
    public void run() {
        log.info("Starting KafkaAnomalyToMetricMapper");
        anomalyConsumer.subscribe(Collections.singletonList(anomalyTopic));
        boolean continueProcessing = true;

        // See Kafka: The Definitive Guide, pp. 86 ff.
        while (continueProcessing) {
            try {
                pollAnomalyTopic();
            } catch (WakeupException e) {
                log.info("Stopping KafkaAnomalyToMetricMapper");
                anomalyConsumer.close();
                metricProducer.flush();
                metricProducer.close();
                continueProcessing = false;
            } catch (Exception e) {
                log.error("Error processing records", e);
            }
        }
    }

    private void pollAnomalyTopic() {
        val anomalyRecords = anomalyConsumer.poll(POLL_PERIOD);
        val numConsumed = anomalyRecords.count();

        log.trace("Read {} anomaly records from topic={}", numConsumed, anomalyTopic);
//        recordsConsumed.increment(numConsumed);

        int numProduced = 0;
        for (val anomalyRecord : anomalyRecords) {
            val anomalyMMD = anomalyRecord.value();
            val anomalyResult = anomalyMMD.getAnomalyResult();
            val anomalyLevel = anomalyResult.getAnomalyLevel();
            if (anomalyLevel == AnomalyLevel.WEAK || anomalyLevel == AnomalyLevel.STRONG) {
                val metricDataRecord = toMetricDataRecord(anomalyRecord);
                if (metricDataRecord != null) {
                    metricProducer.send(metricDataRecord);
                    log.info("Sent: metricDataRecord={}", metricDataRecord);
                    numProduced++;
                }
            }
        }

        log.trace("Wrote {} metricData records to topic={}", numProduced, metricTopic);
//        recordsProduced.increment(numProduced);

        if (anomalyRecords.isEmpty()) {
            return;
        }

        val anomaly0 = anomalyRecords.iterator().next().value();
        val timestamp = anomaly0.getMetricData().getTimestamp() * 1000L;
        val timeDelay = System.currentTimeMillis() - timestamp;
        log.trace("timeDelay={}", timeDelay);
//        delayTimer.record(Duration.ofMillis(timeDelay));
    }

    private ProducerRecord<String, MetricData> toMetricDataRecord(
            ConsumerRecord<String, MappedMetricData> anomalyRecord) {

        assert (anomalyRecord != null);

        val mmd = anomalyRecord.value();
        val metricData = mmd.getMetricData();
        val timestampMillis = metricData.getTimestamp() * 1000L;

        val newMetricData = mapper.toMetricData(mmd);
        if (newMetricData == null) {
            return null;
        }

        val newMetricDef = newMetricData.getMetricDefinition();
        val newMetricId = getMetricId(newMetricDef);
        if (newMetricId == null) {
            return null;
        }

        return new ProducerRecord<>(metricTopic, null, timestampMillis, newMetricId, newMetricData);
    }

    private String getMetricId(MetricDefinition metricDef) {
        // Calling metricTankIdFactory.getId() fails when the metric definition contains tags having values that are
        // null or empty, or contain semicolons. We do see this in production. Hence this check. Would be better though
        // if we can limit or eliminate such metric definitions since we'd like to avoid unnecessary exceptions.
        try {
            return metricTankIdFactory.getId(metricDef);
        } catch (IllegalArgumentException e) {
            log.warn("IllegalArgumentException: message={}, newMetricDef={}", e.getMessage(), metricDef);
            return null;
        }
    }
}
