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

import com.expedia.adaptivealerting.core.OutlierLevel;
import com.expedia.www.haystack.commons.entities.MetricPoint;

import static com.expedia.adaptivealerting.core.OutlierLevel.NORMAL;
import static com.expedia.adaptivealerting.core.OutlierLevel.STRONG;
import static com.expedia.adaptivealerting.core.OutlierLevel.WEAK;
import static com.expedia.adaptivealerting.core.util.AssertUtil.isTrue;

/**
 * Outlier detector with one-sided constant thresholds for weak and strong outliers.
 *
 * @author Willie Wheeler
 */
public class ConstantThresholdOutlierDetector extends AbstractOutlierDetector {
    public static final int LEFT_TAILED = 0;
    public static final int RIGHT_TAILED = 1;
    
    private final int tail;
    private final float strongThreshold;
    private final float weakThreshold;
    
    // TODO Support two-tailed strategy.
    
    /**
     * Builds a constant-threshold anomaly detector that performs anomaly tests in the specified tail. For left-tailed
     * detectors we expect strongThreshold &lt;= weakThreshold, and for right-tailed detectors we expect
     * weakThreshold &lt;= strongThreshold.
     *  @param tail            Either LEFT_TAILED or RIGHT_TAILED.
     * @param strongThreshold Large outlier threshold.
     * @param weakThreshold   Small outlier threshold.
     */
    public ConstantThresholdOutlierDetector(int tail, float strongThreshold, float weakThreshold) {
        if (tail == LEFT_TAILED) {
            isTrue(strongThreshold <= weakThreshold, "Left-tailed detector requires strongThreshold <= weakThreshold");
        } else if (tail == RIGHT_TAILED) {
            isTrue(weakThreshold <= strongThreshold, "Right-tailed detector requires weakThreshold <= strongThreshold");
        } else {
            throw new IllegalArgumentException("tail must be either LEFT_TAILED or RIGHT_TAILED");
        }
        
        this.tail = tail;
        this.weakThreshold = weakThreshold;
        this.strongThreshold = strongThreshold;
    }
    
    public int getTail() {
        return tail;
    }
    
    public Float getWeakThreshold() {
        return weakThreshold;
    }
    
    public Float getStrongThreshold() {
        return strongThreshold;
    }
    
    @Override
    public MetricPoint classify(MetricPoint metricPoint) {
        final float value = metricPoint.value();
        if (tail == LEFT_TAILED) {
            final OutlierLevel level = evaluateLeftTailed(value);
            return tag(metricPoint, level, null, null, null, strongThreshold, weakThreshold);
        } else { // right-tailed til we handle two-tailed
            final OutlierLevel level = evaluateRightTailed(value);
            return tag(metricPoint, level, null, strongThreshold, weakThreshold, null, null);
        }
    }
    
    private OutlierLevel evaluateLeftTailed(double value) {
        if (value <= strongThreshold) {
            return STRONG;
        } else if (value <= weakThreshold) {
            return WEAK;
        } else {
            return NORMAL;
        }
    }
    
    private OutlierLevel evaluateRightTailed(double value) {
        if (value >= strongThreshold) {
            return STRONG;
        } else if (value >= weakThreshold) {
            return WEAK;
        } else {
            return NORMAL;
        }
    }
}
