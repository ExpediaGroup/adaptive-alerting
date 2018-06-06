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

import com.expedia.adaptivealerting.anomdetect.ConstantThresholdAnomalyDetector;
import com.expedia.adaptivealerting.kafka.util.AppUtil;
import com.expedia.adaptivealerting.kafka.util.DetectorUtil;
import com.expedia.www.haystack.commons.kstreams.app.StreamsRunner;
import com.typesafe.config.Config;
import org.apache.kafka.streams.StreamsBuilder;

import static com.expedia.adaptivealerting.anomdetect.ConstantThresholdAnomalyDetector.RIGHT_TAILED;

public class KafkaConstantThresholdOutlierDetector {

    public static void main(String[] args) {
        Config appConfig = AppUtil.getAppConfig("constant-detector");

        final StreamsBuilder builder = DetectorUtil.createDetectorStreamsBuilder(
                appConfig.getString("topic"),
                id -> new ConstantThresholdAnomalyDetector(RIGHT_TAILED, 0.99f, 0.95f)
        );

        StreamsRunner streamsRunner = AppUtil.createStreamsRunner(appConfig, builder);
        AppUtil.launchStreamRunner(streamsRunner);
    }
}
