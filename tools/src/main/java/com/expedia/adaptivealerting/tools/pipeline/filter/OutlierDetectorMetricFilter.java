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
package com.expedia.adaptivealerting.tools.pipeline.filter;

import com.expedia.adaptivealerting.core.OutlierDetector;
import com.expedia.adaptivealerting.core.OutlierLevel;
import com.expedia.www.haystack.commons.entities.MetricPoint;

import static com.expedia.adaptivealerting.core.util.AssertUtil.notNull;
import static com.expedia.adaptivealerting.core.util.MetricPointUtil.classify;

/**
 * Metric filter that applies an outlier detector to the metric.
 *
 * @author Willie Wheeler
 */
public final class OutlierDetectorMetricFilter extends AbstractMetricFilter {
    private final OutlierDetector outlierDetector;
    
    public OutlierDetectorMetricFilter(OutlierDetector outlierDetector) {
        notNull(outlierDetector, "outlierDetector can't be null");
        this.outlierDetector = outlierDetector;
    }
    
    @Override
    public void next(MetricPoint metricPoint) {
        notNull(metricPoint, "metricPoint can't be null");
        final OutlierLevel outlierLevel = outlierDetector.classify(metricPoint);
        publish(classify(metricPoint, outlierLevel));
    }
}
