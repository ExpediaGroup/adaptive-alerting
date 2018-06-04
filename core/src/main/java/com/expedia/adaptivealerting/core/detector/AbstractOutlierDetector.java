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
import scala.collection.immutable.Map;

import static com.expedia.adaptivealerting.core.util.MetricPointTags.*;

public abstract class AbstractOutlierDetector implements OutlierDetector {
    
    protected MetricPoint tag(
            MetricPoint metricPoint,
            OutlierLevel outlierLevel,
            Double prediction,
            Double weakThresholdUpper,
            Double weakThresholdLower,
            Double strongThresholdUpper,
            Double strongThresholdLower) {
    
        Map tags = metricPoint.tags();
        
        if (outlierLevel != null) {
            tags = tags.updated(OUTLIER_LEVEL, outlierLevel.name());
        }
        if (prediction != null) {
            tags = tags.updated(PREDICTION, prediction.floatValue());
        }
        if (weakThresholdUpper != null) {
            tags = tags.updated(WEAK_THRESHOLD_UPPER, weakThresholdUpper.floatValue());
        }
        if (weakThresholdLower != null) {
            tags = tags.updated(WEAK_THRESHOLD_LOWER, weakThresholdLower.floatValue());
        }
        if (strongThresholdUpper != null) {
            tags = tags.updated(STRONG_THRESHOLD_UPPER, strongThresholdUpper.floatValue());
        }
        if (strongThresholdLower != null) {
            tags = tags.updated(STRONG_THRESHOLD_LOWER, strongThresholdLower.floatValue());
        }
    
        return new MetricPoint(
                metricPoint.metric(),
                metricPoint.type(),
                tags,
                metricPoint.value(),
                metricPoint.epochTimeInSeconds());
    }
}
