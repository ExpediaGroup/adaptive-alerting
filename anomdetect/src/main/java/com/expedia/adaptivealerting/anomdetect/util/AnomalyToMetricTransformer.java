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
package com.expedia.adaptivealerting.anomdetect.util;

import com.expedia.adaptivealerting.core.anomaly.AnomalyResult;
import com.expedia.metrics.MetricData;
import com.expedia.metrics.MetricDefinition;
import com.expedia.metrics.TagCollection;
import lombok.val;

import java.util.HashMap;
import java.util.HashSet;

import static com.expedia.adaptivealerting.core.util.AssertUtil.notNull;

/**
 * Transforms an anomaly into a metric. We do this to feed anomalies back into the metric ingest for visualization.
 *
 * @author Willie Wheeler
 */
public class AnomalyToMetricTransformer {
    
    public MetricData transform(AnomalyResult anomalyResult) {
        notNull(anomalyResult, "anomalyResult can't be null");
        
        val metricData = anomalyResult.getMetricData();
        val metricDef = metricData.getMetricDefinition();
        val tags = metricDef.getTags();
        val vTags = tags.getV();
        val kvTags = tags.getKv();
        val value = metricData.getValue();
        val timestamp = metricData.getTimestamp();
        
        if (vTags.contains(AnomalyConstants.ANOMALY)) {
            throw new IllegalArgumentException("Metric can't contain '" + AnomalyConstants.ANOMALY + " ' value tag");
        }
        
        if (kvTags.containsKey(AnomalyConstants.DETECTOR_UUID)) {
            throw new IllegalArgumentException("Metric can't contain '" + AnomalyConstants.DETECTOR_UUID + "' key/value tag");
        }
        
        val newVTags = new HashSet<>(vTags);
        newVTags.add(AnomalyConstants.ANOMALY);
        
        val newKVTags = new HashMap<>(kvTags);
        newKVTags.put(AnomalyConstants.DETECTOR_UUID, anomalyResult.getDetectorUUID().toString());
        
        val newTags = new TagCollection(newKVTags, newVTags);
        val newMetricDef = new MetricDefinition(newTags);
        
        return new MetricData(newMetricDef, value, timestamp);
    }
}
