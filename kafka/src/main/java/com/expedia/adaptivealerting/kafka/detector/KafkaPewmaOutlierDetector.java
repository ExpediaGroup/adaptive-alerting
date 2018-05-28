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
import com.expedia.adaptivealerting.core.OutlierLevel;
import com.expedia.adaptivealerting.core.detector.PewmaOutlierDetector;
import com.expedia.adaptivealerting.kafka.util.DetectorUtil;
import com.expedia.www.haystack.commons.entities.MetricPoint;
import com.expedia.www.haystack.commons.entities.MetricType;
import com.expedia.www.haystack.commons.kstreams.serde.metricpoint.MetricTankSerde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.KStream;
import scala.collection.immutable.Map$;

import java.time.Instant;
import java.util.Properties;

/**
 * Kafka Streams application for the EWMA outlier detector.
 *
 * @author Willie Wheeler
 */
public class KafkaPewmaOutlierDetector {
    
    public static void main(String[] args) {
        
        // FIXME Create a map of these (see KafkaEwmaOutlierDetector for more details).
        final OutlierDetector detector = new PewmaOutlierDetector(0.05, 1.0, 2.0, 3.0, 100.0);

        DetectorUtil.startStreams(detector, "pewma-outlier-detector", "pewma-metics");
    }
}
