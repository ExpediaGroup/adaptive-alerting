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
import com.expedia.adaptivealerting.core.data.MappedMpoint;
import com.expedia.adaptivealerting.core.data.Mpoint;
import com.expedia.adaptivealerting.core.util.AssertUtil;
import com.expedia.adaptivealerting.core.util.MetricUtil;
import com.expedia.www.haystack.commons.entities.MetricPoint;

import static com.expedia.adaptivealerting.core.anomaly.AnomalyLevel.*;
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
public class CusumAnomalyDetector extends AbstractAnomalyDetector {

    public static final int LEFT_TAILED = 0;
    public static final int RIGHT_TAILED = 1;
    public static final int TWO_TAILED = 2;
    public static final double STD_DEV_DIVISOR = 1.128;

    private final int tail;

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
    private int warmUpPeriod;

    /**
     * Local cumulative sum on the high side. SH
     */
    private double sumHigh;

    /**
     * Local cumulative sum on the low side. SL
     */
    private double sumLow;

    /**
     * Strong outlier threshold, in sigmas.
     */
    private final double strongThresholdSigmas;

    /**
     * Weak outlier threshold, in sigmas.
     */
    private final double weakThresholdSigmas;

    /**
     * Local target value.
     */
    private double targetValue;
    
    /**
     * Local slack param value.
     */
    private double slackParam;

    /**
     * Creates a new CUSUM detector with left tail (tail=0), initValue = 0.0, slackParam = 0.5, warmUpPeriod = 25,
     * weakThresholdSigmas = 3.0, strongThresholdSigmas = 4.0 and targetValue = 0.0
     */
    public CusumAnomalyDetector() {
        this(0, 0.0, 0.5, 25, 3.0, 4.0, 0.0);
    }

    /**
     * Creates a new CUSUM detector. Initial mean is given by initValue and initial variance is 0.
     *
     * @param tail
     *            Either LEFT_TAILED, RIGHT_TAILED or TWO_TAILED
     * @param initValue
     *            Initial observation, used to set the first mean estimate.
     * @param slackParam
     *            Slack param to calculate slack value k where k = slack param * stdev
     * @param warmUpPeriod
     *            Warm up period value. Minimum no of data points required before it can be used for actual anomaly
     *            detection.
     * @param weakThresholdSigmas
     *            Weak outlier threshold, in sigmas.
     * @param strongThresholdSigmas
     *            Strong outlier threshold, in sigmas.
     * @param targetValue
     *            User defined target value
     */
    public CusumAnomalyDetector(
            int tail,
            double initValue,
            double slackParam,
            int warmUpPeriod,
            double weakThresholdSigmas,
            double strongThresholdSigmas,
            double targetValue) {
        this.tail = tail;
        this.prevValue = initValue;
        this.slackParam = slackParam;
        this.warmUpPeriod = warmUpPeriod;
        this.movingRange = 0.0;
        this.totalDataPoints = 1;
        this.sumHigh = 0.0;
        this.sumLow = 0.0;
        this.weakThresholdSigmas = weakThresholdSigmas;
        this.strongThresholdSigmas = strongThresholdSigmas;
        this.targetValue = targetValue;
    }

    public double getTargetValue() {
        return targetValue;
    }

    public double getSumHigh() {
        return sumHigh;
    }

    public double getSumLow() {
        return sumLow;
    }

    public double getWeakThresholdSigmas() {
        return weakThresholdSigmas;
    }

    public double getStrongThresholdSigmas() {
        return strongThresholdSigmas;
    }

    public int getWarmUpPeriod() {
        return warmUpPeriod;
    }

    public double getSlackParam() {
        return slackParam;
    }

    @Override
    public AnomalyResult classify(MetricPoint metricPoint) {
        AssertUtil.notNull(metricPoint, "metricPoint can't be null");

        final double observed = metricPoint.value();
        this.movingRange += abs(prevValue - observed);
        final double averageMovingRange = getAverageMovingRange();
        final double dist = abs(observed - targetValue);
        final double stdDev = averageMovingRange / STD_DEV_DIVISOR;
        final double slack = slackParam * stdDev;
        final double weakThreshold = weakThresholdSigmas * stdDev;
        final double strongThreshold = strongThresholdSigmas * stdDev;

        this.sumHigh = Math.max(0, sumHigh + observed - (targetValue + slack));
        this.sumLow = Math.min(0, sumLow + observed - (targetValue - slack));
        this.prevValue = observed;
        this.totalDataPoints++;

        Double weakThresholdUpper = null;
        Double weakThresholdLower = null;
        Double strongThresholdUpper = null;
        Double strongThresholdLower = null;
        AnomalyLevel anomalyLevel = null;

        if (totalDataPoints <= warmUpPeriod) {
            anomalyLevel = UNKNOWN;
        } else {
            anomalyLevel = NORMAL;
            switch (tail) {
            case LEFT_TAILED:
                weakThresholdLower = -weakThreshold;
                strongThresholdLower = -strongThreshold;
                if (sumLow <= strongThresholdLower) {
                    anomalyLevel = STRONG;
                    resetSums();
                } else if (sumLow <= weakThresholdLower) {
                    anomalyLevel = WEAK;
                }
                break;
            case RIGHT_TAILED:
                weakThresholdUpper = weakThreshold;
                strongThresholdUpper = strongThreshold;
                if (sumHigh >= strongThresholdUpper) {
                    anomalyLevel = STRONG;
                    resetSums();
                } else if (sumHigh > weakThresholdUpper) {
                    anomalyLevel = WEAK;
                }
                break;
            case TWO_TAILED:
                weakThresholdLower = -weakThreshold;
                strongThresholdLower = -strongThreshold;
                weakThresholdUpper = weakThreshold;
                strongThresholdUpper = strongThreshold;
                if (sumHigh >= strongThreshold || sumLow <= strongThreshold) {
                    anomalyLevel = STRONG;
                    resetSums();
                } else if (sumHigh > weakThreshold || sumLow <= weakThreshold) {
                    anomalyLevel = WEAK;
                }
                break;
            default:
                throw new IllegalStateException("Illegal tail: " + tail);
            }
        }
        final Mpoint mpoint = MetricUtil.toMpoint(metricPoint);
        
        // FIXME These settings aren't consistent with the current visualization approach.
        // We probably need to make fewer assumptions about the algo in the AnomalyResult class, and support different
        // result approaches. [WLW]
        final AnomalyResult result = new AnomalyResult();
        result.setMetricDefinition(mpoint.getMetricDefinition());
        result.setDetectorId(this.getId());
        result.setEpochSecond(mpoint.getEpochTimeInSeconds());
        result.setObserved(observed);
        result.setPredicted(targetValue);
        result.setWeakThresholdUpper(weakThresholdUpper);
        result.setWeakThresholdLower(weakThresholdLower);
        result.setStrongThresholdUpper(strongThresholdUpper);
        result.setStrongThresholdLower(strongThresholdLower);
        result.setAnomalyScore(dist);
        result.setAnomalyLevel(anomalyLevel);
        return result;
    }
    
    @Override
    public MappedMpoint classify(MappedMpoint mappedMpoint) {
        throw new UnsupportedOperationException("Not yet implemented");
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
