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
package com.expedia.adaptivealerting.anomdetect.control;

import com.expedia.adaptivealerting.anomdetect.AbstractAnomalyDetector;
import com.expedia.adaptivealerting.core.anomaly.AnomalyLevel;
import com.expedia.adaptivealerting.core.anomaly.AnomalyResult;
import com.expedia.adaptivealerting.core.data.MappedMetricData;
import com.expedia.adaptivealerting.core.util.AssertUtil;
import com.expedia.metrics.MetricData;

import static com.expedia.adaptivealerting.core.anomaly.AnomalyLevel.*;

/**
 * Outlier detector with one-sided constant thresholds for weak and strong outliers.
 *
 * @author Willie Wheeler
 */
public class ConstantThresholdAnomalyDetector extends AbstractAnomalyDetector {
    public static final int LEFT_TAILED = 0;
    public static final int RIGHT_TAILED = 1;
    
    private final int tail;
    private final double strongThreshold;
    private final double weakThreshold;
    
    // TODO Support two-tailed strategy.
    
    /**
     * Builds a constant-threshold anomaly detector that performs anomaly tests in the specified tail. For left-tailed
     * detectors we expect strongThreshold &lt;= weakThreshold, and for right-tailed detectors we expect
     * weakThreshold &lt;= strongThreshold.
     *
     * @param tail            Either LEFT_TAILED or RIGHT_TAILED.
     * @param strongThreshold Large outlier threshold.
     * @param weakThreshold   Small outlier threshold.
     */
    public ConstantThresholdAnomalyDetector(int tail, double strongThreshold, double weakThreshold) {
        if (tail == LEFT_TAILED) {
            AssertUtil.isTrue(strongThreshold <= weakThreshold, "Left-tailed detector requires strongThreshold <= weakThreshold");
        } else if (tail == RIGHT_TAILED) {
            AssertUtil.isTrue(weakThreshold <= strongThreshold, "Right-tailed detector requires weakThreshold <= strongThreshold");
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
    
    public double getWeakThreshold() {
        return weakThreshold;
    }
    
    public double getStrongThreshold() {
        return strongThreshold;
    }
    
    @Override
    public String toString() {
        return "ConstantThresholdAnomalyDetector{" +
                "tail=" + tail +
                ", weakThreshold=" + weakThreshold +
                ", strongThreshold=" + strongThreshold +
                '}';
    }
    
    @Override
    protected AnomalyResult toAnomalyResult(MappedMetricData mappedMetricData) {
        final MetricData metricData = mappedMetricData.getMetricData();
        final double observed = metricData.getValue();
        
        Double weakThresholdUpper = null;
        Double weakThresholdLower = null;
        Double strongThresholdUpper = null;
        Double strongThresholdLower = null;
        AnomalyLevel anomalyLevel = NORMAL;
        
        if (tail == LEFT_TAILED) {
            weakThresholdLower = weakThreshold;
            strongThresholdLower = strongThreshold;
            if (observed <= strongThreshold) {
                anomalyLevel = STRONG;
            } else if (observed <= weakThreshold) {
                anomalyLevel = WEAK;
            }
        } else if (tail == RIGHT_TAILED) {
            weakThresholdUpper = weakThreshold;
            strongThresholdUpper = strongThreshold;
            if (observed >= strongThreshold) {
                anomalyLevel = STRONG;
            } else if (observed >= weakThreshold) {
                anomalyLevel = WEAK;
            }
        } else {
            throw new IllegalStateException("Illegal tail: " + tail);
        }
        
        final AnomalyResult result = new AnomalyResult();
        result.setMetricDefinition(metricData.getMetricDefinition());
        result.setEpochSecond(metricData.getTimestamp());
        result.setObserved(observed);
        result.setWeakThresholdUpper(weakThresholdUpper);
        result.setWeakThresholdLower(weakThresholdLower);
        result.setStrongThresholdUpper(strongThresholdUpper);
        result.setStrongThresholdLower(strongThresholdLower);
        result.setAnomalyLevel(anomalyLevel);
        return result;
    }
}
