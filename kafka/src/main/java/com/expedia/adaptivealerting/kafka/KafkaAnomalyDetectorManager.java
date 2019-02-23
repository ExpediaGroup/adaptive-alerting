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
import com.expedia.adaptivealerting.core.anomaly.AnomalyLevel;
import com.expedia.adaptivealerting.core.anomaly.AnomalyResult;
import com.expedia.adaptivealerting.core.data.MappedMetricData;
import com.expedia.adaptivealerting.kafka.util.DetectorUtil;
import com.expedia.adaptivealerting.core.util.ErrorUtil;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.KStream;

import static com.expedia.adaptivealerting.core.util.AssertUtil.notNull;

/**
 * Kafka streams wrapper around {@link DetectorManager}.
 */
@Slf4j
public final class KafkaAnomalyDetectorManager extends AbstractStreamsApp {
    private static final String CK_AD_MANAGER = "ad-manager";
    
    private final DetectorManager manager;
    
    public static void main(String[] args) {
        val config = new TypesafeConfigLoader(CK_AD_MANAGER).loadMergedConfig();
        val saConfig = new StreamsAppConfig(config);
        val detectorSource = DetectorUtil.buildDetectorSource(config);
        val manager = new DetectorManager(detectorSource);
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
        val inboundTopic = config.getInboundTopic();
        val outboundTopic = config.getOutboundTopic();
    
        log.info("Initializing: inboundTopic={}, outboundTopic={}", inboundTopic, outboundTopic);
        
        val builder = new StreamsBuilder();
        final KStream<String, MappedMetricData> stream = builder.stream(inboundTopic);
        stream
                .filter((key, mmd) -> mmd != null)
                .filter((key, mmd) -> hasManagedDetector(mmd))
                .mapValues(this::toAnomaly)
                .filter((key, mmd) -> mmd != null)
                .filter((key, mmd) -> isWeakOrStrongAnomaly(mmd))
                .to(outboundTopic);
        return builder.build();
    }
    
    private boolean hasManagedDetector(MappedMetricData mmd) {
        assert mmd != null;
        return manager.getDetectorTypes().contains(mmd.getDetectorType());
    }
    
    private MappedMetricData toAnomaly(MappedMetricData mmd) {
        assert mmd != null;
        AnomalyResult anomalyResult = null;
        try {
            anomalyResult = manager.classify(mmd);
            log.info("anomalyResult={}", anomalyResult);
        } catch (Exception e) {
            log.error("Classification error: mmd={}, error={}", mmd, ErrorUtil.fullExceptionDetails(e));
        }
        return anomalyResult == null ? null : new MappedMetricData(mmd, anomalyResult);
    }
    
    private boolean isWeakOrStrongAnomaly(MappedMetricData mmd) {
        assert mmd != null;
        val level = mmd.getAnomalyResult().getAnomalyLevel();
        return level == AnomalyLevel.WEAK || level == AnomalyLevel.STRONG;
    }
}
