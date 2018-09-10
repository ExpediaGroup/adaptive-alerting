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
package com.expedia.adaptivealerting.anomdetect;

import com.expedia.adaptivealerting.core.anomaly.AnomalyResult;
import com.expedia.adaptivealerting.core.data.MappedMetricData;
import com.expedia.www.haystack.commons.entities.MetricPoint;

import java.util.UUID;

/**
 * Anomaly detector interface.
 *
 * @author Willie Wheeler
 */
public interface AnomalyDetector {
    
    UUID getUuid();
    
    MappedMetricData classify(MappedMetricData mappedMetricData);
    
    
    // ========================================
    // Deprecated
    // ========================================
    
    /**
     * Classifies the given metric point.
     *
     * @param metricPoint Metric point.
     * @return Anomaly classification result, with supporting data such as the prediction, anomaly score and various
     * thresholds.
     * @deprecated Deprecated in favor of {@link #classify(MappedMetricData)}.
     */
    @Deprecated
    AnomalyResult classify(MetricPoint metricPoint);
}
