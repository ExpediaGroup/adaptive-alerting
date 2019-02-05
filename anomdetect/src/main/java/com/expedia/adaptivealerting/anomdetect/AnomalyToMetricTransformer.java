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

import com.expedia.adaptivealerting.anomdetect.util.AnomalyConstants;
import com.expedia.adaptivealerting.core.anomaly.AnomalyResult;
import com.expedia.metrics.MetricData;
import com.expedia.metrics.MetricDefinition;
import com.expedia.metrics.TagCollection;
import lombok.val;

import java.util.Collections;
import java.util.HashMap;

import static com.expedia.adaptivealerting.core.util.AssertUtil.notNull;

/**
 * <p>
 * Transforms an anomaly into a metric. We do this to feed anomalies back into the metric ingest for visualization.
 * </p>
 * <p>
 * The current implementation intentionally avoids value tags since Metrictank doesn't support them. In the future we
 * might generalize this.
 * </p>
 *
 * @author Willie Wheeler
 */
public class AnomalyToMetricTransformer {
    
    /**
     * Key/value tag key for metrics representing an anomaly. The value must be the detector UUID.
     */
    public static String AA_DETECTOR_UUID = "aa_detector_uuid";
    
    public MetricData transform(AnomalyResult anomalyResult) {
        notNull(anomalyResult, "anomalyResult can't be null");
        
        val metricData = anomalyResult.getMetricData();
        val metricDef = metricData.getMetricDefinition();
        val tags = metricDef.getTags();
        val kvTags = tags.getKv();
        val value = metricData.getValue();
        val timestamp = metricData.getTimestamp();
        
        if (kvTags.containsKey(AA_DETECTOR_UUID)) {
            throw new IllegalArgumentException("Metric can't contain '" + AA_DETECTOR_UUID + "' key/value tag");
        }
        
        val newKVTags = new HashMap<>(kvTags);
        newKVTags.put(AA_DETECTOR_UUID, anomalyResult.getDetectorUUID().toString());
        
        val newKey = metricDef.getKey();
        val newTags = new TagCollection(newKVTags, Collections.EMPTY_SET);
        val newMeta = new TagCollection(Collections.EMPTY_MAP);
        val newMetricDef = new MetricDefinition(newKey, newTags, newMeta);
        
        return new MetricData(newMetricDef, value, timestamp);
    }
}
