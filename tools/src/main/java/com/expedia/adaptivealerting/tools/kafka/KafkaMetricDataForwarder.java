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
package com.expedia.adaptivealerting.tools.kafka;

import com.expedia.adaptivealerting.tools.pipeline.source.MetricSource;
import com.expedia.metrics.MetricData;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import static com.expedia.adaptivealerting.core.util.AssertUtil.notNull;

/**
 * Forwards data from a metric source to a single Kafka topic.
 *
 * @author Willie Wheeler
 */
public final class KafkaMetricDataForwarder {
    
    public KafkaMetricDataForwarder(
            MetricSource metricSource,
            Producer<String, MetricData> kafkaProducer,
            String topicName) {
        
        notNull(metricSource, "metricSource can't be null");
        notNull(kafkaProducer, "kafkaProducer can't be null");
        
        metricSource.addSubscriber(metricData -> kafkaProducer.send(new ProducerRecord<>(topicName, null, metricData)));
        metricSource.start();
    }
}
