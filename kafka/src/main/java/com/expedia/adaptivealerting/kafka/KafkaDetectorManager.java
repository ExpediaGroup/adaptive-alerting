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

import com.codahale.metrics.SharedMetricRegistries;
import com.expedia.adaptivealerting.anomdetect.DetectorManager;
import com.expedia.adaptivealerting.anomdetect.detect.breakout.BreakoutDetectorResult;
import com.expedia.adaptivealerting.anomdetect.detect.DetectorResult;
import com.expedia.adaptivealerting.anomdetect.detect.MappedMetricData;
import com.expedia.adaptivealerting.anomdetect.detect.outlier.OutlierDetectorResult;
import com.expedia.adaptivealerting.kafka.util.ConfigUtil;
import com.expedia.adaptivealerting.kafka.util.DetectorUtil;
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

import static com.expedia.adaptivealerting.anomdetect.util.AssertUtil.notNull;

/**
 * Kafka app to connect a {@link DetectorManager} to an input metric topic and output anomaly
 * topics. This is a consumer/producer app instead of a Kafka Streams app since we need to
 * send messages to multiple output topics, which isn't possible with a Kafka Streams app.
 */
@Slf4j
public class KafkaDetectorManager implements Runnable {

    // TODO Rename this to detector-manager. But have to coordinate that with Terraform scripts. [WLW]
    private static final String APP_ID = "ad-manager";

    private static final String METRIC_CONSUMER = "metric-consumer";
    private static final String ANOMALY_PRODUCER = "anomaly-producer";
    private static final String TOPIC = "topic";
    private static final String OUTLIER_TOPIC = "outlier-topic";
    private static final String BREAKOUT_TOPIC = "breakout-topic";
    private static final long POLL_PERIOD = 1000L;

    @Getter
    private final DetectorManager detectorManager;

    @Getter
    private final Consumer<String, MappedMetricData> metricConsumer;

    @Getter
    private final Producer<String, MappedMetricData> anomalyProducer;

    @Getter
    private final String metricTopic;

    @Getter
    private final String outlierTopic;

    @Getter
    private final String breakoutTopic;

    // Cleaned code coverage
    // https://reflectoring.io/100-percent-test-coverage/
    @Generated
    public static void main(String[] args) {
        val config = new TypesafeConfigLoader(APP_ID).loadMergedConfig();
        val manager = buildManager(config);
        manager.run();
    }

    // Extracted for unit testing
    static KafkaDetectorManager buildManager(Config config) {
        val metricRegistry = SharedMetricRegistries.getOrCreate(APP_ID);
        val detectorSource = DetectorUtil.buildDetectorSource(config);
        val detectorManager = new DetectorManager(detectorSource, config, metricRegistry);

        val metricConsumerConfig = config.getConfig(METRIC_CONSUMER);
        val metricConsumerProps = ConfigUtil.toConsumerConfig(metricConsumerConfig);
        val metricConsumer = new KafkaConsumer<String, MappedMetricData>(metricConsumerProps);
        val metricConsumerTopic = metricConsumerConfig.getString(TOPIC);

        val anomalyProducerConfig = config.getConfig(ANOMALY_PRODUCER);
        val anomalyProducerProps = ConfigUtil.toProducerConfig(anomalyProducerConfig);
        val anomalyProducer = new KafkaProducer<String, MappedMetricData>(anomalyProducerProps);
        val anomalyProducerOutlierTopic = anomalyProducerConfig.getString(OUTLIER_TOPIC);
        val anomalyProducerBreakoutTopic = anomalyProducerConfig.getString(BREAKOUT_TOPIC);

        return new KafkaDetectorManager(
                detectorManager,
                metricConsumer,
                anomalyProducer,
                metricConsumerTopic,
                anomalyProducerOutlierTopic,
                anomalyProducerBreakoutTopic);
    }

    public KafkaDetectorManager(
            DetectorManager detectorManager,
            Consumer<String, MappedMetricData> metricConsumer,
            Producer<String, MappedMetricData> anomalyProducer,
            String metricTopic,
            String outlierTopic,
            String breakoutTopic) {

        notNull(detectorManager, "detectorManager can't be null");
        notNull(metricConsumer, "metricConsumer can't be null");
        notNull(anomalyProducer, "anomalyProducer can't be null");
        notNull(metricTopic, "metricTopic can't be null");
        notNull(outlierTopic, "outlierTopic can't be null");
        notNull(breakoutTopic, "breakoutTopic can't be null");

        this.detectorManager = detectorManager;
        this.metricConsumer = metricConsumer;
        this.anomalyProducer = anomalyProducer;
        this.metricTopic = metricTopic;
        this.outlierTopic = outlierTopic;
        this.breakoutTopic = breakoutTopic;
    }

    @Override
    public void run() {
        log.info("Starting KafkaDetectorManager");
        metricConsumer.subscribe(Collections.singletonList(metricTopic));
        boolean continueProcessing = true;

        // See Kafka: The Definitive Guide, pp. 86 ff.
        while (continueProcessing) {
            try {
                processMetrics();
            } catch (WakeupException e) {
                log.info("Stopping KafkaDetectorManager");
                metricConsumer.close();
                anomalyProducer.flush();
                anomalyProducer.close();
                continueProcessing = false;
            } catch (Exception e) {
                log.error("Error processing records", e);
            }
        }
    }

    private void processMetrics() {
        val metricRecords = metricConsumer.poll(POLL_PERIOD);
        val numConsumed = metricRecords.count();

        log.trace("Read {} metric records from topic={}", numConsumed, metricTopic);

        int numProduced = 0;
        for (val metricRecord : metricRecords) {
            val anomalyRecord = toAnomalyRecord(metricRecord);
            anomalyProducer.send(anomalyRecord);
            log.info("Sent: anomalyRecord={}", anomalyRecord);
            numProduced++;
        }

        // We write to both the outlier and the breakout topics, so we don't report a
        // single topic name here. If we want to break counts up by topic we can do that.
        // [WLW]
        log.trace("Wrote {} anomaly records", numProduced);

        if (metricRecords.isEmpty()) {
            return;
        }
    }

    private ProducerRecord<String, MappedMetricData> toAnomalyRecord(
            ConsumerRecord<String, MappedMetricData> metricRecord) {

        val metricMMD = metricRecord.value();
        val metricData = metricMMD.getMetricData();
        val timestampMillis = metricData.getTimestamp() * 1000L;
        val key = metricRecord.key();

        val detectorResult = detectorManager.detect(metricMMD);

        // This can happen if we end up doing no-ops external detectors.
        if (detectorResult == null) {
            return null;
        }

        val outputTopic = getOutputTopic(detectorResult);
        val anomalyMMD = new MappedMetricData(metricMMD, detectorResult);

        return new ProducerRecord<>(outputTopic, null, timestampMillis, key, anomalyMMD);
    }

    private String getOutputTopic(DetectorResult result) {

        // TODO Right now I'm violating the open-closed principle here. As we currently
        //  have only two general anomaly types, this seems like the right approach--it's
        //  simple and isolated. If we end up adding many more, it might make sense to
        //  invest in something more configurable. [WLW]
        val resultClass = result.getClass();
        if (OutlierDetectorResult.class.isAssignableFrom(resultClass)) {
            return outlierTopic;
        } else if (BreakoutDetectorResult.class.isAssignableFrom(resultClass)) {
            return breakoutTopic;
        } else {
            throw new RuntimeException("Unknown DetectorResult class: " + resultClass);
        }
    }
}
