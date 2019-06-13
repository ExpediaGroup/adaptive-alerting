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

import com.expedia.adaptivealerting.anomdetect.util.AssertUtil;
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
     * Key/value tag key for anomalous metrics. The value must be the detector UUID.
     */
    public static String AA_DETECTOR_UUID = "aa_detector_uuid";

    /**
     * Key/value tag key for anomalous metrics. The value must be the anomaly level.
     */
    public static String AA_ANOMALY_LEVEL = "aa_anomaly_level";

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
     * @param anomaly anomaly result to transform into a metric
     * @return the anomaly result as metric data
     */
    public MetricData toMetricData(MappedMetricData anomaly) {
        if (anomaly == null) {
            return null;
        }

        val metricData = anomaly.getMetricData();
        val metricDef = metricData.getMetricDefinition();
        val tags = metricDef.getTags();
        val kvTags = tags.getKv();

        // Reverting back to returning null instead of generating an exception. We get to define the transform we want
        // here, and I don't see an advantage to generating an exception when the proper transform at the Kafka level
        // is to transform it into a null too. It reduces the burden on the client. [WLW]
        if (kvTags.containsKey(AA_DETECTOR_UUID) || kvTags.containsKey(AA_ANOMALY_LEVEL)) {
            return null;
        }

        val detectorUuid = anomaly.getDetectorUuid();
        val anomalyResult = anomaly.getAnomalyResult();
        val anomalyLevel = anomalyResult.getAnomalyLevel();

        AssertUtil.notNull(detectorUuid, "detectorUuid can't be null");
        AssertUtil.notNull(anomalyLevel, "anomalyLevel can't be null");

        val newKVTags = new HashMap<>(kvTags);
        newKVTags.put(AA_DETECTOR_UUID, detectorUuid.toString());
        newKVTags.put(AA_ANOMALY_LEVEL, anomalyLevel.toString());

        val newKey = metricDef.getKey();
        val newTags = new TagCollection(newKVTags, Collections.EMPTY_SET);
        val newMeta = new TagCollection(Collections.EMPTY_MAP);
        val newMetricDef = new MetricDefinition(newKey, newTags, newMeta);
        val value = metricData.getValue();
        val timestamp = metricData.getTimestamp();

        return new MetricData(newMetricDef, value, timestamp);
    }
}
