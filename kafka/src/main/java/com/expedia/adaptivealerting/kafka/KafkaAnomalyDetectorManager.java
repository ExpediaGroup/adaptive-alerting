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
 *
 * @author David Sutherland
 * @author Willie Wheeler
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
        val detectorTypes = manager.getDetectorTypes();
    
        log.info("Initializing: inboundTopic={}, outboundTopic={}", inboundTopic, outboundTopic);
        
        val builder = new StreamsBuilder();
        final KStream<String, MappedMetricData> stream = builder.stream(inboundTopic);
        stream
                .filter((key, mappedMetricData) -> mappedMetricData != null
                        && detectorTypes.contains(mappedMetricData.getDetectorType()))
                .mapValues(mappedMetricData -> {
                    log.trace("Processing mappedMetricData: {}", mappedMetricData);
                    
                    // TODO Not sure why we would get null here--mappedMetricData are mapped to models. But in fact we
                    // are seeing this occur so let's handle it and investigate the cause. [WLW]
                    AnomalyResult anomalyResult = null;
                    try {
                        anomalyResult = manager.classify(mappedMetricData);
                    } catch (Exception e) {
                        log.error(
                                "Encountered error while classifying {}. {}",
                                mappedMetricData,
                                ErrorUtil.fullExceptionDetails(e)
                        );
                    }
                    
                    log.info("anomalyResult={}", anomalyResult);
                    
                    return anomalyResult == null ? null : new MappedMetricData(mappedMetricData, anomalyResult);
                })
                .filter((key, mappedMetricData) -> mappedMetricData != null)
                .to(outboundTopic);
        return builder.build();
    }

}
