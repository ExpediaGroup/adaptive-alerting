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
package com.expedia.adaptivealerting.tools.kafka;

import com.expedia.adaptivealerting.core.data.MetricFrame;
import com.expedia.adaptivealerting.core.data.io.MetricFrameLoader;
import com.expedia.adaptivealerting.tools.pipeline.source.MetricFrameMetricSource;
import com.expedia.adaptivealerting.tools.pipeline.source.MetricSource;
import com.expedia.metrics.MetricData;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import static com.expedia.adaptivealerting.core.util.AssertUtil.notNull;

/**
 * Forwards data from a metric source to a single Kafka topic.
 *
 * @author Willie Wheeler
 */
@Slf4j
public final class KafkaMetricDataForwarder {
    
    public static void main(String[] args) throws IOException {
        final OptionParser parser = new OptionParser();
        parser.accepts("m").withRequiredArg().ofType(String.class);
        parser.accepts("d").withRequiredArg().ofType(String.class);
        parser.accepts("t").withRequiredArg().ofType(String.class);
        
        final OptionSet options = parser.parse(args);
        final File metricFile = new File((String) options.valueOf("m"));
        final File dataFile = new File((String) options.valueOf("d"));
        final String topicName = (String) options.valueOf("t");
        
        final MetricFrame metricFrame = MetricFrameLoader.loadCsv(metricFile, dataFile, true);
        
        // TODO Make the metric name and publication period optional params? [WLW]
        final MetricFrameMetricSource metricSource = new MetricFrameMetricSource(metricFrame, "metric", 1000L);
    
        Properties props = new Properties();
        props.put("bootstrap.servers", "aa.local:9092");
        props.put("acks", "all");
        props.put("retries", 0);
//        props.put("batch.size", 16384);
        props.put("linger.ms", 1);
        props.put("buffer.memory", 33554432);
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "com.expedia.adaptivealerting.kafka.serde.JsonPojoSerializer");
//        props.put("value.serializer", "com.expedia.adaptivealerting.kafka.serde.MetricDataSerde$DataSerializer");
        Producer<String, MetricData> producer = new KafkaProducer<>(props);
        
        new KafkaMetricDataForwarder(metricSource, producer, topicName);
    }
    
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
