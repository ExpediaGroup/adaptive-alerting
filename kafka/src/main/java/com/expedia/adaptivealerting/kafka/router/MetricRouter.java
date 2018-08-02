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
import com.expedia.www.haystack.commons.entities.TagKeys;
import com.expedia.www.haystack.commons.kstreams.app.StreamsRunner;
import com.typesafe.config.Config;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.KStream;

import static com.expedia.adaptivealerting.kafka.KafkaConfigProps.INBOUND_TOPIC;
import static com.expedia.adaptivealerting.kafka.KafkaConfigProps.METRIC_ROUTER;
import static com.expedia.adaptivealerting.kafka.KafkaConfigProps.OUTBOUND_TOPIC;

/**
 * @deprecated Use {@link com.expedia.adaptivealerting.kafka.mapper.KafkaAnomalyDetectorMapper} instead.
 */
@Deprecated
public final class MetricRouter {

    public static void main(String[] args) {
        Config appConfig = AppUtil.getAppConfig(METRIC_ROUTER);
        AppUtil.launchStreamRunner(new StreamRunnerBuilder().build(appConfig));
    }

    public static class StreamRunnerBuilder extends BaseStreamRunnerBuilder {
        
        @Override
        public StreamsRunner build(Config appConfig) {
            final StreamsBuilder builder = createStreamsBuilder(appConfig);

            return createStreamsRunner(appConfig, builder);
        }

        private static StreamsBuilder createStreamsBuilder(Config appConfig) {
            final String inboundTopic = appConfig.getString(INBOUND_TOPIC);
            final String outboundTopic = appConfig.getString(OUTBOUND_TOPIC);
            
            final StreamsBuilder builder = new StreamsBuilder();
            final KStream<String, MetricPoint> metrics = builder.stream(inboundTopic);
    
            // TODO Create MappedMpoints and push them to the single outbound topic. [WLW]
//            metrics.map(mpoint -> enrichWithDetectorInfo(mpoint)).to(outboundTopic);
            
            // TODO Get rid of these once we have the single outbound topic. [WLW]
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
            return "duration".equals(metricPoint.metric())
              && "airboss".equals(getTagVal(metricPoint, TagKeys.SERVICE_NAME_KEY()))
              && "PurchaseV3".equals(getTagVal(metricPoint, TagKeys.OPERATION_NAME_KEY()))
              && "FiveMinute".equalsIgnoreCase(getTagVal(metricPoint, TagKeys.INTERVAL_KEY()))
              && "*_99".equalsIgnoreCase(getTagVal(metricPoint, TagKeys.STATS_KEY()));
        }

        private static String getTagVal(MetricPoint metricPoint, String tagKey) {
            return metricPoint.tags() != null ? scala.collection.JavaConverters
              .mapAsJavaMapConverter(metricPoint.tags()).asJava().get(tagKey) : "";
        }

        private static boolean isPewma(String key, MetricPoint metricPoint) {
            return "pewma".equals(metricPoint.metric());
        }
        
        private static boolean isAquila(String key, MetricPoint metricPoint) {
            return "booking-series".equals(metricPoint.metric());
        }
    }
}
