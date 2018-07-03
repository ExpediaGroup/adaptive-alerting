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

import com.expedia.adaptivealerting.kafka.util.AppUtil;
import com.expedia.adaptivealerting.kafka.util.BaseStreamRunnerBuilder;
import com.expedia.www.haystack.commons.entities.MetricPoint;
import com.expedia.www.haystack.commons.kstreams.app.StreamsRunner;
import com.typesafe.config.Config;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.KStream;

// TODO Isolate the infrastructure-independent logic and move it to the core package. [WLW]

public class MetricRouter {

    public static void main(String[] args) {
        Config appConfig = AppUtil.getAppConfig("metric-router");
        AppUtil.launchStreamRunner(new StreamRunnerBuilder().build(appConfig));
    }

    public static class StreamRunnerBuilder extends BaseStreamRunnerBuilder {
        
        @Override
        public StreamsRunner build(Config appConfig) {
            final StreamsBuilder builder = createStreamsBuilder(appConfig);

            return createStreamsRunner(appConfig, builder);
        }

        private static StreamsBuilder createStreamsBuilder(Config appConfig) {
            final StreamsBuilder builder = new StreamsBuilder();
            final KStream<String, MetricPoint> metrics = builder.stream(appConfig.getString("topic"));
    
            // TODO These are all hardcoded. Replace with data-driven routing. [WLW]
            metrics.filter(StreamRunnerBuilder::isConstant).to("constant-metrics");
            metrics.filter(StreamRunnerBuilder::isEwma).to("ewma-metrics");
            metrics.filter(StreamRunnerBuilder::isPewma).to("pewma-metrics");
            metrics.filter(StreamRunnerBuilder::isAquila).to("aquila-metrics");
            
            return builder;
        }
        
        private static boolean isConstant(String key, MetricPoint metricPoint) {
            return "latency".equals(metricPoint.metric());
        }

        private static boolean isEwma(String key, MetricPoint metricPoint) {
            return "duration".equals(metricPoint.metric());
        }

        private static boolean isPewma(String key, MetricPoint metricPoint) {
            return "pewma".equals(metricPoint.metric());
        }
        
        private static boolean isAquila(String key, MetricPoint metricPoint) {
            return "booking-series".equals(metricPoint.metric());
        }
    }
}
