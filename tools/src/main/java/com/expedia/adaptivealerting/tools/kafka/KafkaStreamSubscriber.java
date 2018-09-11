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

import com.expedia.adaptivealerting.tools.pipeline.util.MetricDataSubscriber;
import com.expedia.metrics.MetricData;
import com.expedia.www.haystack.commons.kstreams.serde.metricpoint.MetricPointSerializer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.StreamsConfig;

import java.util.Properties;

import static com.expedia.adaptivealerting.core.util.AssertUtil.notNull;

/**
 * @author Willie Wheeler
 */
public class KafkaStreamSubscriber implements MetricDataSubscriber {
    private final String topicName;
    private final KafkaProducer<String, MetricData> producer;
    
    /**
     * Creates a data generator callback to publish messages to the given topic.
     *
     * @param topicName Kafka topic name.
     */
    public KafkaStreamSubscriber(String topicName) {
        notNull(topicName, "topicName can't be null");
        
        this.topicName = topicName;
        
        // TODO Externalize
        final Properties conf = new Properties();
        conf.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        conf.put("key.serializer", StringSerializer.class.getName());
        conf.put("value.serializer", MetricPointSerializer.class.getName());
    
        this.producer = new KafkaProducer<>(conf);
    }
    
    public void next(MetricData metricData) {
        producer.send(new ProducerRecord<String, MetricData>(topicName, null, metricData));
    }
}
