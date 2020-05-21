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
package com.expedia.adaptivealerting.kafka.detectorrunner;

import com.expedia.adaptivealerting.anomdetect.detect.MappedMetricData;
import com.expedia.adaptivealerting.kafka.TypesafeConfigLoader;
import com.expedia.adaptivealerting.kafka.util.ConfigUtil;
import com.typesafe.config.Config;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.springframework.stereotype.Component;

import java.util.Properties;

@Component
public class AnomalyProducer {

    private static final String ANOMALY_PRODUCER = "anomaly-producer";
    private static String APP = "detector-runner";

    private KafkaProducer<String, MappedMetricData> producer;

    private Config anomalyProducerConfig;

    public AnomalyProducer() {
        Config config = new TypesafeConfigLoader(APP).loadMergedConfig();
        anomalyProducerConfig = config.getConfig(ANOMALY_PRODUCER);
        Properties anomalyProducerProps = ConfigUtil.toProducerConfig(anomalyProducerConfig);
        producer = new KafkaProducer<>(anomalyProducerProps);
    }

    public KafkaProducer<String, MappedMetricData> getProducer() {
        return producer;
    }

    public Config getAnomalyProducerConfig() {
        return anomalyProducerConfig;
    }
}
