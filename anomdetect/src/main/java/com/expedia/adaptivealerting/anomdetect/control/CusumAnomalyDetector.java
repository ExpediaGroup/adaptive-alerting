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
import com.expedia.metrics.MetricData;
import lombok.Getter;
import lombok.ToString;

import java.util.UUID;

import static com.expedia.adaptivealerting.core.anomaly.AnomalyLevel.*;
import static com.expedia.adaptivealerting.core.util.AssertUtil.notNull;
import static java.lang.Math.abs;

/**
 * <p>
 * Anomaly detector based on the cumulative sum. This is an online algorithm, meaning that it updates the thresholds
 * incrementally as new data comes in.
 * https://www.spcforexcel.com/knowledge/variable-control-charts/keeping-process-target-cusum-charts
 * </p>
 *
 * @author kashah
 */
@ToString
public final class CusumAnomalyDetector extends AbstractAnomalyDetector {
    public enum Type {
        LEFT_TAILED,
        RIGHT_TAILED,
        TWO_TAILED
    }
    
    private static final double STD_DEV_DIVISOR = 1.128;
    
    @Getter
    private Type type;
    
    /**
     * Local Moving range. Used to calculate standard deviation.
     */
    private double movingRange;
    
    /**
     * Local previous value.
     */
    private double prevValue;
    
    /**
     * Local total no of received data points.
     */
    private int totalDataPoints;
    
    /**
     * Local warm up period value. Minimum no of data points required before it can be used for actual anomaly
     * detection.
     */
    @Getter
    private int warmUpPeriod;
    
    /**
     * Local cumulative sum on the high side. SH
     */
    @Getter
    private double sumHigh;
    
    /**
     * Local cumulative sum on the low side. SL
     */
    @Getter
    private double sumLow;
    
    /**
     * Strong anomaly threshold, in sigmas.
     */
    @Getter
    private double strongSigmas;
    
    /**
     * Weak anomaly threshold, in sigmas.
     */
    @Getter
    private double weakSigmas;
    
    /**
     * Local target value.
     */
    private double targetValue;
    
    /**
     * Local slack param value.
     */
    private double slackParam;
    
    /**
     * Creates a new CUSUM detector with left type, initValue = 0.0, slackParam = 0.5, warmUpPeriod = 25,
     * weakSigmas = 3.0, strongSigmas = 4.0 and targetValue = 0.0
     */
    public CusumAnomalyDetector(UUID uuid) {
        this(uuid, Type.LEFT_TAILED, 0.0, 0.5, 25, 3.0, 4.0, 0.0);
    }
    
    /**
     * Creates a new CUSUM detector. Initial mean is given by initValue and initial variance is 0.
     *
     * @param uuid         Detector UUID
     * @param type         Either LEFT_TAILED, RIGHT_TAILED or TWO_TAILED
     * @param initValue    Initial observation, used to set the first mean estimate.
     * @param slackParam   Slack param to calculate slack value k where k = slack param * stdev
     * @param warmUpPeriod Warm up period value. Minimum no of data points required before it can be used for
     *                     actual anomaly detection.
     * @param weakSigmas   Weak outlier threshold, in sigmas.
     * @param strongSigmas Strong outlier threshold, in sigmas.
     * @param targetValue  User defined target value
     */
    public CusumAnomalyDetector(
            UUID uuid,
            Type type,
            double initValue,
            double slackParam,
            int warmUpPeriod,
            double weakSigmas,
            double strongSigmas,
            double targetValue) {
        
        super(uuid);
        
        this.type = type;
        this.prevValue = initValue;
        this.slackParam = slackParam;
        this.warmUpPeriod = warmUpPeriod;
        this.movingRange = 0.0;
        this.totalDataPoints = 1;
        this.sumHigh = 0.0;
        this.sumLow = 0.0;
        this.weakSigmas = weakSigmas;
        this.strongSigmas = strongSigmas;
        this.targetValue = targetValue;
    }
    
    @Override
    public AnomalyResult classify(MetricData metricData) {
        notNull(metricData, "metricData can't be null");
        
        final double observed = metricData.getValue();
        
        this.movingRange += abs(prevValue - observed);
        final double averageMovingRange = getAverageMovingRange();
        final double stdDev = averageMovingRange / STD_DEV_DIVISOR;
        final double slack = slackParam * stdDev;
        final double weakThreshold = weakSigmas * stdDev;
        final double strongThreshold = strongSigmas * stdDev;
        
        this.sumHigh = Math.max(0, sumHigh + observed - (targetValue + slack));
        this.sumLow = Math.min(0, sumLow + observed - (targetValue - slack));
        this.prevValue = observed;
        this.totalDataPoints++;
        
        // TODO Evaluate whether we can use an AnomalyThresholds here. [WLW]
        Double upperStrong;
        Double upperWeak;
        Double lowerStrong;
        Double lowerWeak;
        AnomalyLevel level;
        
        if (totalDataPoints <= warmUpPeriod) {
            level = UNKNOWN;
        } else {
            level = NORMAL;
            switch (type) {
                case LEFT_TAILED:
                    lowerWeak = -weakThreshold;
                    lowerStrong = -strongThreshold;
                    if (sumLow <= lowerStrong) {
                        level = STRONG;
                        resetSums();
                    } else if (sumLow <= lowerWeak) {
                        level = WEAK;
                    }
                    break;
                case RIGHT_TAILED:
                    upperWeak = weakThreshold;
                    upperStrong = strongThreshold;
                    if (sumHigh >= upperStrong) {
                        level = STRONG;
                        resetSums();
                    } else if (sumHigh > upperWeak) {
                        level = WEAK;
                    }
                    break;
                case TWO_TAILED:
                    if (sumHigh >= strongThreshold || sumLow <= strongThreshold) {
                        level = STRONG;
                        resetSums();
                    } else if (sumHigh > weakThreshold || sumLow <= weakThreshold) {
                        level = WEAK;
                    }
                    break;
                default:
                    throw new IllegalStateException("Illegal type: " + type);
            }
        }
        
        return anomalyResult(metricData, level);
    }
    
    private double getAverageMovingRange() {
        if (totalDataPoints > 1) {
            return movingRange / (totalDataPoints - 1);
        }
        return movingRange;
    }
    
    private void resetSums() {
        this.sumHigh = 0.0;
        this.sumLow = 0.0;
    }
}
