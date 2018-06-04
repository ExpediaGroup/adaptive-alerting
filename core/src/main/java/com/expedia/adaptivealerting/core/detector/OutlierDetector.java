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
package com.expedia.adaptivealerting.core.detector;

import com.expedia.www.haystack.commons.entities.MetricPoint;

/**
 * Outlier detector interface.
 *
 * @author Willie Wheeler
 */
public interface OutlierDetector {
    
    /**
     * Classifies the given metric point.
     *
     * @param metricPoint Metric point.
     * @return Outlier classification result, with supporting data such as the prediction, outlier score and various
     * thresholds.
     */
    OutlierDetectorResult classify(MetricPoint metricPoint);
    
    /**
     * Classifies the given metric point, and enriches it with the classification.
     *
     * @param metricPoint Metric point.
     * @return A new copy of the metric point, with classification and additional information attached.
     * @deprecated It is incorrect to add the outlier classification result and supporting data to the metric point as
     * tags. Metric 2.0 tags are intended to identify the metric itself, not the metric values. Use
     * {@link #classify(MetricPoint)} instead.
     */
    @Deprecated
    MetricPoint classifyAndEnrich(MetricPoint metricPoint);
}
