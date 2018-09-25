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
package com.expedia.adaptivealerting.kafka.mapper;

import com.expedia.adaptivealerting.anomdetect.AnomalyDetectorMapper;
import com.expedia.adaptivealerting.anomdetect.util.HttpClientWrapper;
import com.expedia.adaptivealerting.anomdetect.util.ModelServiceConnector;
import com.expedia.adaptivealerting.core.data.MappedMetricData;
import com.expedia.adaptivealerting.kafka.AbstractKafkaApp;
import com.expedia.adaptivealerting.kafka.serde.JsonPojoSerde;
import com.expedia.adaptivealerting.kafka.util.AppUtil;
import com.expedia.metrics.MetricData;
import com.typesafe.config.Config;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;

import java.util.Set;
import java.util.stream.Collectors;

import static com.expedia.adaptivealerting.core.util.AssertUtil.notNull;
import static com.expedia.adaptivealerting.kafka.KafkaConfigProps.*;

/**
 * Kafka wrapper around {@link AnomalyDetectorMapper}.
 *
 * @author David Sutherland
 * @author Willie Wheeler
 */
public final class KafkaAnomalyDetectorMapper extends AbstractKafkaApp {
    private AnomalyDetectorMapper mapper;
    
    public static void main(String[] args) {
        final Config appConfig = AppUtil.getAppConfig(ANOMALY_DETECTOR_MAPPER);
        final HttpClientWrapper httpClient = new HttpClientWrapper();
        final String uriTemplate = appConfig.getString(MODEL_SERVICE_URI_TEMPLATE);
        final ModelServiceConnector modelServiceConnector = new ModelServiceConnector(httpClient, uriTemplate);
        final AnomalyDetectorMapper mapper = new AnomalyDetectorMapper(modelServiceConnector);
        new KafkaAnomalyDetectorMapper(appConfig, mapper).start();
    }
    
    public KafkaAnomalyDetectorMapper(Config appConfig, AnomalyDetectorMapper mapper) {
        super(appConfig);
        notNull(mapper, "mapper can't be null");
        this.mapper = mapper;
    }
    
    @Override
    protected StreamsBuilder streamsBuilder() {
        final StreamsBuilder builder = new StreamsBuilder();
        final String inboundTopic = getAppConfig().getString(INBOUND_TOPIC);
        final String outboundTopic = getAppConfig().getString(OUTBOUND_TOPIC);
        final KStream<String, MetricData> stream = builder.stream(inboundTopic);
        
        // This approach forces all detectors for a given metric to reside with a given manager.
//        stream
//                .flatMapValues(metricData -> mapper.map(metricData))
//                .to(outboundTopic);
        
        // This approach allows us to distribute detectors for a given metric across managers.
        stream
                .flatMap((key, metricData) -> {
                        
                            // Convert the <key, mpoint> pair into a set of <detectorUuid, mappedMetricData> pairs.
                            // Each detector UUID comes from its mapped mpoint.
                            final Set<MappedMetricData> mappedMetricDataSet = mapper.map(metricData);
                            return mappedMetricDataSet.stream()
                                    .map(mappedMetricData -> {
                                        final String newKey = mappedMetricData.getDetectorUuid().toString();
                                        return KeyValue.pair(newKey, mappedMetricData);
                                    })
                                    .collect(Collectors.toSet());
                        }
                )
                .to(outboundTopic, Produced.with(new Serdes.StringSerde(), new JsonPojoSerde<>()));
    
        return builder;
    }
}
