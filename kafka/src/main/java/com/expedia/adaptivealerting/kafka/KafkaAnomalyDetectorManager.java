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

import com.expedia.adaptivealerting.anomdetect.DetectorManager;
import com.expedia.adaptivealerting.anomdetect.detector.AnomalyResult;
import com.expedia.adaptivealerting.anomdetect.detector.MappedMetricData;
import com.expedia.adaptivealerting.anomdetect.util.AssertUtil;
import com.expedia.adaptivealerting.anomdetect.util.ErrorUtil;
import com.expedia.adaptivealerting.kafka.util.DetectorUtil;
import lombok.Generated;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.KStream;

import static com.expedia.adaptivealerting.anomdetect.util.AssertUtil.notNull;

/**
 * <p>
 * Kafka streams wrapper around {@link DetectorManager}.
 * </p>
 * <p>
 * We publish all classifications to the output topic. Though AnomalyLevel.NORMAL classifications generate high event
 * volume, downstream consumers user those to reliably detect recovery from an anomalous situation. Anyway this wrapper
 * isn't responsible for domain logic; its responsibility is to adapt the {@link DetectorManager} to Kafka.
 * </p>
 */
@Slf4j
public final class KafkaAnomalyDetectorManager extends AbstractStreamsApp {
    private static final String CK_AD_MANAGER = "ad-manager";

    private final DetectorManager manager;

    // Cleaned code coverage
    // https://reflectoring.io/100-percent-test-coverage/
    @Generated
    public static void main(String[] args) {
        val config = new TypesafeConfigLoader(CK_AD_MANAGER).loadMergedConfig();
        val saConfig = new StreamsAppConfig(config);
        val detectorSource = DetectorUtil.buildDetectorSource(config);
        val manager = new DetectorManager(detectorSource, config);
        new KafkaAnomalyDetectorManager(saConfig, manager).start();
    }

    public KafkaAnomalyDetectorManager(StreamsAppConfig config, DetectorManager manager) {
        super(config);
        notNull(manager, "manager can't be null");
        this.manager = manager;
    }

    @Override
    protected Topology buildTopology() {
        val config = getConfig();
        val inputTopic = config.getInputTopic();
        val outputTopic = config.getOutputTopic();
        log.info("Initializing: inputTopic={}, outputTopic={}", inputTopic, outputTopic);

        val builder = new StreamsBuilder();
        final KStream<String, MappedMetricData> stream = builder.stream(inputTopic);
        stream
                .filter((key, mmd) -> mmd != null)
                .mapValues(this::toAnomalyMmd)
                .filter((key, mmd) -> mmd != null)
                .to(outputTopic);
        return builder.build();
    }

    private MappedMetricData toAnomalyMmd(MappedMetricData mmd) {
        AssertUtil.notNull(mmd, "mappedMetricData can't be null");

        AnomalyResult anomalyResult = null;
        try {
            anomalyResult = manager.classify(mmd);
        } catch (Exception e) {
            log.error("Classification error: mappedMetricData={}, error={}",
                    mmd,
                    ErrorUtil.singleLineExceptionTrace(e));
        }

        if (anomalyResult == null) {
            log.info("anomalyResult=null");
            return null;
        }

        val newMmd = new MappedMetricData(mmd, anomalyResult);
        log.info("produced={}", newMmd);
        return newMmd;
    }
}
