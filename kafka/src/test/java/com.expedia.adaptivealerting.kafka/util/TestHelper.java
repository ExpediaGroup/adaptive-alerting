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
package com.expedia.adaptivealerting.kafka.util;

import com.expedia.adaptivealerting.anomdetect.outlier.AnomalyLevel;
import com.expedia.adaptivealerting.anomdetect.outlier.AnomalyResult;
import com.expedia.adaptivealerting.anomdetect.MappedMetricData;
import com.expedia.metrics.MetricData;
import com.expedia.metrics.MetricDefinition;
import com.expedia.metrics.TagCollection;
import com.github.charithe.kafka.KafkaJunitRule;
import com.google.common.collect.ImmutableMap;
import org.apache.kafka.clients.CommonClientConfigs;

import java.time.Instant;
import java.util.UUID;

import static com.expedia.metrics.MetricDefinition.MTYPE;
import static com.expedia.metrics.MetricDefinition.UNIT;

public class TestHelper {

    public static MappedMetricData newMappedMetricData() {
        TagCollection tags = new TagCollection(ImmutableMap.of(MTYPE, "gauge", UNIT, "metric"));
        MetricDefinition def = new MetricDefinition("latency", tags, TagCollection.EMPTY);
        MetricData data = new MetricData(def, 2.0f, Instant.now().getEpochSecond());
        UUID detectorUUID = UUID.randomUUID();
        MappedMetricData mappedData = new MappedMetricData(data, detectorUUID);
        mappedData.setAnomalyResult(new AnomalyResult(AnomalyLevel.NORMAL));
        return mappedData;
    }

    public static String bootstrapServers(KafkaJunitRule kafka) {
        return kafka.helper()
                .consumerConfig()
                .getProperty(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG);
    }
}
