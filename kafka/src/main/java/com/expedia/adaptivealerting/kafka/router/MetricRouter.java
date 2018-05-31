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
package com.expedia.adaptivealerting.kafka.router;

import com.expedia.adaptivealerting.kafka.util.DetectorUtil;
import com.expedia.www.haystack.commons.entities.MetricPoint;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.KStream;

import java.util.Arrays;
import java.util.Collections;
import java.util.Properties;

public class MetricRouter {

    public static void main(String[] args) {
        final Properties conf = DetectorUtil.getStreamConfig("metric-router");

        final StreamsBuilder builder = new StreamsBuilder();
        final KStream<String, MetricPoint> metrics = builder.stream("metrics");

        metrics.filter(MetricRouter::isConstant).to("constant-metrics");
        metrics.filter(MetricRouter::isEwma).to("ewma-metrics");
        metrics.filter(MetricRouter::isPewma).to("pewma-metrics");

        final Topology topology = builder.build();
        final KafkaStreams streams = new KafkaStreams(topology, conf);

        streams.start();
        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
    }

    // TODO: add real routing conditions
    private static boolean isConstant(String key, MetricPoint metricPoint) {
        return Arrays.asList("latency", "duration").contains(metricPoint.metric());
    }

    private static boolean isEwma(String key, MetricPoint metricPoint) {
        return Collections.singletonList("ewma").contains(metricPoint.metric());
    }

    private static boolean isPewma(String key, MetricPoint metricPoint) {
        return Collections.singletonList("pewma").contains(metricPoint.metric());
    }
}
