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

import com.expedia.adaptivealerting.core.OutlierDetector;
import com.expedia.adaptivealerting.core.OutlierLevel;
import com.expedia.www.haystack.commons.entities.MetricPoint;
import scala.collection.immutable.Map;

import static com.expedia.adaptivealerting.core.util.MetricPointTags.*;

public abstract class AbstractOutlierDetector implements OutlierDetector {
    
    protected MetricPoint tag(
            MetricPoint metricPoint,
            OutlierLevel outlierLevel,
            Float prediction,
            Float upperThresholdStrong,
            Float upperThresholdWeak,
            Float lowerThresholdStrong,
            Float lowerThresholdWeak) {
    
        Map tags = metricPoint.tags();
        
        if (outlierLevel != null) {
            tags = tags.updated(OUTLIER_LEVEL, outlierLevel.name());
        }
        if (prediction != null) {
            tags = tags.updated(PREDICTION, prediction);
        }
        if (upperThresholdStrong != null) {
            tags = tags.updated(UPPER_THRESHOLD_STRONG, upperThresholdStrong);
        }
        if (upperThresholdWeak != null) {
            tags = tags.updated(UPPER_THRESHOLD_WEAK, upperThresholdWeak);
        }
        if (lowerThresholdStrong != null) {
            tags = tags.updated(LOWER_THRESHOLD_STRONG, lowerThresholdStrong);
        }
        if (lowerThresholdWeak != null) {
            tags = tags.updated(LOWER_THRESHOLD_WEAK, lowerThresholdWeak);
        }
    
        return new MetricPoint(
                metricPoint.metric(),
                metricPoint.type(),
                tags,
                metricPoint.value(),
                metricPoint.epochTimeInSeconds());
    }
}
