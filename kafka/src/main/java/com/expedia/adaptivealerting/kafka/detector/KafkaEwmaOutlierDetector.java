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
package com.expedia.adaptivealerting.kafka.detector;

import com.expedia.adaptivealerting.anomdetect.control.EwmaAnomalyDetector;
import com.expedia.adaptivealerting.kafka.util.AppUtil;
import com.expedia.adaptivealerting.kafka.util.BaseStreamRunnerBuilder;
import com.expedia.adaptivealerting.kafka.util.DetectorUtil;
import com.expedia.www.haystack.commons.kstreams.app.StreamsRunner;
import com.typesafe.config.Config;
import org.apache.kafka.streams.StreamsBuilder;

import static com.expedia.adaptivealerting.kafka.KafkaConfigProps.INBOUND_TOPIC;

// TODO Rename to KafkaEwmaAnomalyDetector. [WLW]

/**
 * Kafka Streams application for the EWMA outlier detector.
 *
 * @author Willie Wheeler
 * @deprecated Use KafkaAnomalyDetectorManager instead.
 */
@Deprecated
public final class KafkaEwmaOutlierDetector {

    public static void main(String[] args) {
        Config appConfig = AppUtil.getAppConfig("ewma-detector");
        AppUtil.launchStreamRunner(new StreamRunnerBuilder().build(appConfig));
    }

    public static class StreamRunnerBuilder extends BaseStreamRunnerBuilder {
        @Override
        public StreamsRunner build(Config appConfig) {
            final StreamsBuilder builder = DetectorUtil.createDetectorStreamsBuilder(
                appConfig.getString(INBOUND_TOPIC),
                id -> new EwmaAnomalyDetector(0.8, 3.0, 4.0, 100.0)
            );
            return createStreamsRunner(appConfig, builder);
        }
    }
}
