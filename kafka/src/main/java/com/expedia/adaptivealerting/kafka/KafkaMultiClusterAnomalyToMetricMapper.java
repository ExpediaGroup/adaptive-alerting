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

import com.expedia.adaptivealerting.anomdetect.util.AnomalyToMetricTransformer;
import com.expedia.metrics.metrictank.MetricTankIdFactory;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;

/**
 * @author Willie Wheeler
 */
@Slf4j
public class KafkaMultiClusterAnomalyToMetricMapper {
    
    // TODO Replace with configured values
    private static final String BOOTSTRAP_SERVERS = "localhost:9092";
    private static final String TOPIC = "mdm";
    
    private final AnomalyToMetricTransformer transformer = new AnomalyToMetricTransformer();
    private final MetricTankIdFactory metricTankIdFactory = new MetricTankIdFactory();
    
    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            runProducer(5);
        } else {
            runProducer(Integer.parseInt(args[0]));
        }
    }
    
    public static void runProducer(final int sendMessageCount) throws Exception {
        val producer = createProducer();
        val time = System.currentTimeMillis();
        
        try {
            for (long index = time; index < time + sendMessageCount; index++) {
                // TODO Replace with logic from KafkaAnomalyToMetricMapper [WLW]
                val record = new ProducerRecord<Long, String>(TOPIC, index, "Hi Mom " + index);
                val meta = producer.send(record).get();
                val elapsedTime = System.currentTimeMillis() - time;
                log.info("Sent record(key={}, value={}) meta(partition={}, offset={}) time={}",
                        record.key(), record.value(), meta.partition(), meta.offset(), elapsedTime);
            }
        } finally {
            producer.flush();
            producer.close();
        }
    }
    
    private static Producer<Long, String> createProducer() {
        val props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        props.put(ProducerConfig.CLIENT_ID_CONFIG, "KafkaExampleProducer");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        return new KafkaProducer<>(props);
    }
}
