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

import static com.expedia.adaptivealerting.core.util.AssertUtil.isTrue;

/**
 * Outlier detector with one-sided constant thresholds for weak and strong outliers.
 *
 * @author Willie Wheeler
 */
public class ConstantThresholdOutlierDetector implements OutlierDetector {
    public static final int LEFT_TAILED = 0;
    public static final int RIGHT_TAILED = 1;
    
    private final int tail;
    private final double weakThreshold;
    private final double largeThreshold;
    
    /**
     * Builds a constant-threshold anomaly detector that performs anomaly tests in the specified tail. For left-tailed
     * detectors we expect largeThreshold &lt;= weakThreshold, and for right-tailed detectors we expect
     * weakThreshold &lt;= largeThreshold.
     *
     * @param tail           Either LEFT_TAILED or RIGHT_TAILED.
     * @param smallThreshold Small anomaly threshold.
     * @param largeThreshold Large anomaly threshold.
     */
    public ConstantThresholdOutlierDetector(int tail, double smallThreshold, double largeThreshold) {
        if (tail == LEFT_TAILED) {
            isTrue(largeThreshold <= smallThreshold, "Left-tailed detector requires largeThreshold <= weakThreshold");
        } else if (tail == RIGHT_TAILED) {
            isTrue(smallThreshold <= largeThreshold, "Right-tailed detector required weakThreshold <= largeThreshold");
        } else {
            throw new IllegalArgumentException("tail must be either LEFT_TAILED or RIGHT_TAILED");
        }
        
        this.tail = tail;
        this.weakThreshold = smallThreshold;
        this.largeThreshold = largeThreshold;
    }
    
    public int getTail() {
        return tail;
    }
    
    public Double getWeakThreshold() {
        return weakThreshold;
    }
    
    public Double getLargeThreshold() {
        return largeThreshold;
    }
    
    @Override
    public OutlierLevel classify(MetricPoint metricPoint) {
        final float value = metricPoint.value();
        return tail == LEFT_TAILED ? evaluateLeftTailed(value) : evaluateRightTailed(value);
    }
    
    private OutlierLevel evaluateLeftTailed(double value) {
        if (value <= largeThreshold) {
            return OutlierLevel.STRONG;
        } else if (value <= weakThreshold) {
            return OutlierLevel.WEAK;
        } else {
            return OutlierLevel.NORMAL;
        }
    }
    
    private OutlierLevel evaluateRightTailed(double value) {
        if (value >= largeThreshold) {
            return OutlierLevel.STRONG;
        } else if (value >= weakThreshold) {
            return OutlierLevel.WEAK;
        } else {
            return OutlierLevel.NORMAL;
        }
    }
}
