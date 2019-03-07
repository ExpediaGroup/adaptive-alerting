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
package com.expedia.adaptivealerting.anomdetect;

import com.expedia.adaptivealerting.core.anomaly.AnomalyResult;
import com.expedia.metrics.MetricData;
import com.expedia.metrics.MetricDefinition;
import com.expedia.metrics.TagCollection;
import lombok.val;

import java.util.Collections;
import java.util.HashMap;

/**
 * <p>
 * Transforms an anomaly into a metric. We do this to feed anomalies back into the metric ingest for visualization.
 * </p>
 * <p>
 * The current implementation intentionally avoids value tags since Metrictank doesn't support them. In the future we
 * might generalize this.
 * </p>
 */
public class AnomalyToMetricMapper {

    /**
     * Key/value tag key for metrics representing an anomaly. The value must be the detector UUID.
     */
    public static String AA_DETECTOR_UUID = "aa_detector_uuid";

    /**
     * <p>
     * Transforms the given anomaly result. This method copies the original metric key and tags, and adds a new
     * {@code aa_detector_uuid} tag whose value is the UUID of the detector that performed the classification. It does
     * not copy metadata tags.
     * </p>
     * <p>
     * Returns a null if the anomaly result's metric definition contains the {@code aa_detector_uuid} tag.
     * </p>
     *
     * @param anomalyResult anomaly result to transform into a metric
     * @return the anomaly result as metric data
     */
    public MetricData toMetricData(AnomalyResult anomalyResult) {

        // Just transform null to null. [WLW]
//        notNull(anomalyResult, "anomalyResult can't be null");
        if (anomalyResult == null) {
            return null;
        }

        val metricData = anomalyResult.getMetricData();
        val metricDef = metricData.getMetricDefinition();
        val tags = metricDef.getTags();
        val kvTags = tags.getKv();

        // Reverting back to returning null instead of generating an exception. We get to define the transform we want
        // here, and I don't see an advantage to generating an exception when the proper transform at the Kafka level
        // is to transform it into a null too. It reduces the burden on the client. [WLW]
//        isFalse(kvTags.containsKey(AA_DETECTOR_UUID), "Tag " + AA_DETECTOR_UUID + " not allowed");
        if (kvTags.containsKey(AA_DETECTOR_UUID)) {
            return null;
        }

        val newKVTags = new HashMap<>(kvTags);
        newKVTags.put(AA_DETECTOR_UUID, anomalyResult.getDetectorUUID().toString());

        val newKey = metricDef.getKey();
        val newTags = new TagCollection(newKVTags, Collections.EMPTY_SET);
        val newMeta = new TagCollection(Collections.EMPTY_MAP);
        val newMetricDef = new MetricDefinition(newKey, newTags, newMeta);
        val value = metricData.getValue();
        val timestamp = metricData.getTimestamp();

        return new MetricData(newMetricDef, value, timestamp);
    }
}
