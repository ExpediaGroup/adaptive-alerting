/*
 * Copyright 2018 Expedia Group, Inc.
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
package com.expedia.adaptivealerting.kafka.detector;

import com.expedia.adaptivealerting.anomdetect.AnomalyDetectorManager;
import com.expedia.adaptivealerting.core.anomaly.AnomalyResult;
import com.expedia.adaptivealerting.core.data.MappedMetricData;
import com.expedia.adaptivealerting.kafka.AbstractKafkaApp;
import com.expedia.adaptivealerting.kafka.util.AppUtil;
import com.typesafe.config.Config;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.KStream;

import static com.expedia.adaptivealerting.core.util.AssertUtil.notNull;
import static com.expedia.adaptivealerting.kafka.KafkaConfigProps.*;

/**
 * Kafka wrapper around {@link AnomalyDetectorManager}.
 *
 * @author David Sutherland
 * @author Willie Wheeler
 */
@Slf4j
public final class KafkaAnomalyDetectorManager extends AbstractKafkaApp {
    private AnomalyDetectorManager manager;
    
    public static void main(String[] args) {
        final Config appConfig = AppUtil.getAppConfig(ANOMALY_DETECTOR_MANAGER);
        final Config detectorsConfig = appConfig.getConfig(DETECTORS);
        final AnomalyDetectorManager manager = new AnomalyDetectorManager(detectorsConfig);
        new KafkaAnomalyDetectorManager(appConfig, manager).start();
    }
    
    public KafkaAnomalyDetectorManager(Config appConfig, AnomalyDetectorManager manager) {
        super(appConfig);
        notNull(manager, "manager can't be null");
        this.manager = manager;
    }
    
    @Override
    protected StreamsBuilder streamsBuilder() {
        final String inboundTopic = getAppConfig().getString(INBOUND_TOPIC);
        final String outboundTopic = getAppConfig().getString(OUTBOUND_TOPIC);
        final StreamsBuilder builder = new StreamsBuilder();
        final KStream<String, MappedMetricData> stream = builder.stream(inboundTopic);
        stream
                .mapValues(mappedMetricData -> {
                    log.info("Processing mappedMetricData: {}", mappedMetricData);
                    
                    // TODO Not sure why we would get null here--mappedMetricData are mapped to models. But in fact we
                    // are seeing this occur so let's handle it and investigate the cause. [WLW]
                    AnomalyResult anomalyResult = null;
                    try {
                        anomalyResult = manager.classify(mappedMetricData);
                    } catch (Exception e) {
                        log.error(
                                "Error while classifying [{}]. mappedMetricData={}",
                                e.getMessage(),
                                mappedMetricData
                        );
                    }
                    
                    log.info("anomalyResult={}", anomalyResult);
                    
                    return anomalyResult == null ? null : new MappedMetricData(mappedMetricData, anomalyResult);
                })
                .filter((key, mappedMetricData) -> mappedMetricData != null)
                .to(outboundTopic);
        return builder;
    }
}
