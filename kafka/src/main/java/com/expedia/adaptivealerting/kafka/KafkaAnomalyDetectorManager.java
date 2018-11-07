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
package com.expedia.adaptivealerting.kafka;

import com.expedia.adaptivealerting.anomdetect.AnomalyDetectorManager;
import com.expedia.adaptivealerting.anomdetect.util.HttpClientWrapper;
import com.expedia.adaptivealerting.anomdetect.util.ModelServiceConnector;
import com.expedia.adaptivealerting.core.anomaly.AnomalyResult;
import com.expedia.adaptivealerting.core.data.MappedMetricData;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.KStream;

/**
 * Kafka streams wrapper around {@link AnomalyDetectorManager}.
 *
 * @author David Sutherland
 * @author Willie Wheeler
 */
@Slf4j
public final class KafkaAnomalyDetectorManager extends AbstractStreamsApp {
    private static final String CK_AD_MANAGER = "ad-manager";
    private static final String CK_MODEL_SERVICE_URI_TEMPLATE = "model-service-uri-template";
    
    private final AnomalyDetectorManager manager;
    
    public static void main(String[] args) {
        val config = StreamsAppConfigLoader.load(CK_AD_MANAGER);
        new KafkaAnomalyDetectorManager(config).start();
    }
    
    public KafkaAnomalyDetectorManager(StreamsAppConfig config) {
        super(config);
        this.manager = buildManager(config);
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
        return builder.build();
    }

    private static AnomalyDetectorManager buildManager(StreamsAppConfig appConfig) {
        val managerConfig = appConfig.getTypesafeConfig();
        val httpClient = new HttpClientWrapper();
        val modelServiceUriTemplate = managerConfig.getString(CK_MODEL_SERVICE_URI_TEMPLATE);
        val connector = new ModelServiceConnector(httpClient, modelServiceUriTemplate);
        return new AnomalyDetectorManager(managerConfig, connector);
    }
}
