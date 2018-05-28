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

import com.expedia.adaptivealerting.core.OutlierDetector;
import com.expedia.adaptivealerting.core.detector.EwmaOutlierDetector;
import com.expedia.adaptivealerting.kafka.util.DetectorUtil;

/**
 * Kafka Streams application for the EWMA outlier detector.
 *
 * @author Willie Wheeler
 */
public class KafkaEwmaOutlierDetector {
    
    public static void main(String[] args) {
        
        // FIXME Create a map of these, rather than just using a single one across the board.
        // This will be a little involved because currently we are using metric names like "latency", and there are
        // many distinct series with that name. So we have to decide whether we want names to be unique or whether we
        // want to use tags for unique names.
        // But fine for now as this is just a demo. [WLW]
        final OutlierDetector detector = new EwmaOutlierDetector(0.8, 2.0, 3.0, 100.0);

        DetectorUtil.startStreams(detector, "ewma-outlier-detector", "ewma-metrics");
    }
}
