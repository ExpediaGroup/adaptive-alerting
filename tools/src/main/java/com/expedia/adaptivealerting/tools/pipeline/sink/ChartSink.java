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
package com.expedia.adaptivealerting.tools.pipeline.sink;

import com.expedia.adaptivealerting.tools.pipeline.MetricSink;
import com.expedia.www.haystack.commons.entities.MetricPoint;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;

import static com.expedia.adaptivealerting.core.util.AssertUtil.notNull;
import static com.expedia.adaptivealerting.core.util.MetricPointTags.LOWER_THRESHOLD_STRONG;
import static com.expedia.adaptivealerting.core.util.MetricPointTags.UPPER_THRESHOLD_STRONG;
import static com.expedia.adaptivealerting.core.util.MetricPointUtil.thresholdValue;

public class ChartSink implements MetricSink {
    private final TimeSeries predictedUpper;
    private final TimeSeries predictedLower;
    private final TimeSeries observed;
    
    // FIXME For now, just impose timestamps.
    // Later update to use actual metric point timestamps.
    private Second currentSecond;
    
    public ChartSink(TimeSeries predictedUpper, TimeSeries predictedLower, TimeSeries observed) {
        notNull(predictedUpper, "predictedUpper can't be null");
        notNull(predictedLower, "predictedLower can't be null");
        notNull(observed, "observed can't be null");
        
        this.predictedUpper = predictedUpper;
        this.predictedLower = predictedLower;
        this.observed = observed;
    }
    
    @Override
    public void next(MetricPoint metricPoint) {
        this.currentSecond = (currentSecond == null ? new Second() : (Second) currentSecond.next());
        final float value = metricPoint.value();
        final float upperThresholdStrong = thresholdValue(metricPoint, UPPER_THRESHOLD_STRONG);
        final float lowerThresholdStrong = thresholdValue(metricPoint, LOWER_THRESHOLD_STRONG);
        
        predictedUpper.add(currentSecond, upperThresholdStrong);
        predictedLower.add(currentSecond, lowerThresholdStrong);
        observed.add(currentSecond, value);
    }
}
