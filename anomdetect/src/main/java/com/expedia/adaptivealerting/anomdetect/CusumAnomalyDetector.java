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
package com.expedia.adaptivealerting.anomdetect;

import static com.expedia.adaptivealerting.core.anomaly.AnomalyLevel.NORMAL;
import static com.expedia.adaptivealerting.core.anomaly.AnomalyLevel.STRONG;
import static com.expedia.adaptivealerting.core.anomaly.AnomalyLevel.WEAK;
import static java.lang.Math.abs;
import static java.lang.Math.sqrt;
import com.expedia.adaptivealerting.core.anomaly.AnomalyLevel;
import com.expedia.adaptivealerting.core.anomaly.AnomalyResult;
import com.expedia.adaptivealerting.core.data.Mpoint;
import com.expedia.adaptivealerting.core.util.AssertUtil;
import com.expedia.adaptivealerting.core.util.MetricPointUtil;
import com.expedia.www.haystack.commons.entities.MetricPoint;

/**
 * <p>
 * Anomaly detector based on the cumulative sum. This is an online algorithm, meaning that it updates the thresholds
 * incrementally as new data comes in.
 * https://www.spcforexcel.com/knowledge/variable-control-charts/keeping-process-target-cusum-charts
 * </p>
 *
 * @author kashah
 */
public class CusumAnomalyDetector implements AnomalyDetector {

    public static final int LEFT_TAILED = 0;
    public static final int RIGHT_TAILED = 1;
    public static final int TWO_TAILED = 2;

    private final int tail;

    /**
     * Smoothing param.
     */
    private double alpha;

    /**
     * Local mean estimate. If user doesn't specify the target then this becomes the target value.
     */
    private double mean;

    /**
     * Local cumulative sum on the high side. SH
     */
    private double highCusum;

    /**
     * Local cumulative sum on the low side. SL
     */
    private double lowCusum;

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
     * Local variance estimate.
     */
    private double variance;

    /**
     * Creates a new CUSUM detector with left tail (tail=0), alpha = 0.15, initValue = 0.0, weakThresholdSigmas = 3.0,
     * strongThresholdSigmas = 4.0 and targetValue = 0.0
     */
    public CusumAnomalyDetector() {
        this(0, 0.15, 0.0, 3.0, 4.0, 0.0);
    }

    /**
     * Creates a new CUSUM detector. Initial mean is given by initValue and initial variance is 0.
     *
     *@param tail
     *            Either LEFT_TAILED, RIGHT_TAILED or TWO_TAILED
     * @param alpha
     *            Smoothing parameter.
     * @param initValue
     *            Initial observation, used to set the first mean estimate.
     * @param targetValue
     *            User defined target value
     */
    public CusumAnomalyDetector(
            int tail,
            double alpha,
            double initValue,
            double weakThresholdSigmas,
            double strongThresholdSigmas,
            double targetValue) {
        this.tail = tail;
        this.alpha = alpha;
        this.mean = initValue;
        this.variance = 0.0;
        this.highCusum = 0.0;
        this.lowCusum = 0.0;
        this.weakThresholdSigmas = weakThresholdSigmas;
        this.strongThresholdSigmas = strongThresholdSigmas;
        this.targetValue = targetValue;
    }

    public double getAlpha() {
        return alpha;
    }

    public double getTargetValue() {
        return targetValue;
    }

    public double getMean() {
        return mean;
    }

    public double getVariance() {
        return variance;
    }

    public double getHighCusum() {
        return highCusum;
    }

    public double getLowCusum() {
        return lowCusum;
    }

    public double getWeakThresholdSigmas() {
        return weakThresholdSigmas;
    }

    public double getStrongThresholdSigmas() {
        return strongThresholdSigmas;
    }

    public int getTail() {
        return tail;
    }

    @Override
    public AnomalyResult classify(MetricPoint metricPoint) {
        AssertUtil.notNull(metricPoint, "metricPoint can't be null");

        final double observed = metricPoint.value();   
        setTargetValue(mean);
     
        final double dist = abs(observed - targetValue);
        final double stdDev = sqrt(variance);
        final double slackValue = 0.5 * stdDev;
        final double weakThreshold = weakThresholdSigmas * stdDev;
        final double strongThreshold = strongThresholdSigmas * stdDev;
        
        this.highCusum = Math.max(0, highCusum + observed - (targetValue + slackValue));
        this.lowCusum = Math.min(0, lowCusum + observed - (targetValue - slackValue));
        
        Double weakThresholdUpper = null;
        Double weakThresholdLower = null;
        Double strongThresholdUpper = null;
        Double strongThresholdLower = null;

        AnomalyLevel anomalyLevel = NORMAL;
        switch (tail) {
        case LEFT_TAILED:
            weakThresholdLower = -weakThreshold;
            strongThresholdLower = -strongThreshold;
            if (lowCusum <= strongThresholdLower) {
                anomalyLevel = STRONG;
            } else if (lowCusum <= weakThresholdLower) {
                anomalyLevel = WEAK;
            }
            break;
        case RIGHT_TAILED:
            weakThresholdUpper = weakThreshold;
            strongThresholdUpper = strongThreshold;
            if (highCusum >= strongThresholdUpper) {
                anomalyLevel = STRONG;
            } else if (highCusum > weakThresholdUpper) {
                anomalyLevel = WEAK;
            }
            break;
        case TWO_TAILED:
            weakThresholdLower = -weakThreshold;
            strongThresholdLower = -strongThreshold;
            weakThresholdUpper = weakThreshold;
            strongThresholdUpper = strongThreshold;
            if (highCusum >= strongThreshold && lowCusum <= strongThreshold) {
                anomalyLevel = STRONG;
            } else if (highCusum > weakThreshold && lowCusum <= weakThreshold) {
                anomalyLevel = WEAK;
            }
            break;
        default:
            throw new IllegalStateException("Illegal tail: " + tail);
        }
        
        final Mpoint mpoint = MetricPointUtil.toMpoint(metricPoint);
        final AnomalyResult result = new AnomalyResult();
        result.setMetric(mpoint.getMetric());
        result.setDetectorId(this.getId());
        result.setEpochSecond(mpoint.getInstant().getEpochSecond());
        result.setObserved(observed);
        result.setPredicted(targetValue);
        result.setWeakThresholdUpper(weakThresholdUpper);
        result.setWeakThresholdLower(weakThresholdLower);
        result.setStrongThresholdUpper(strongThresholdUpper);
        result.setStrongThresholdLower(strongThresholdLower);
        result.setAnomalyScore(dist);
        result.setAnomalyLevel(anomalyLevel);

        updateMeanAndStdDev(observed);

        return result;
    }

    private void setTargetValue(double mean) {
        if (targetValue == 0.0) {
            this.targetValue = mean;
        }
    }

    private void updateMeanAndStdDev(double value) {

        // https://en.wikipedia.org/wiki/Moving_average#Exponentially_weighted_moving_variance_and_standard_deviation
        // http://people.ds.cam.ac.uk/fanf2/hermes/doc/antiforgery/stats.pdf
        final double diff = value - mean;
        final double incr = alpha * diff;

        this.mean = mean + incr;

        // Welford's algorithm for computing the variance online
        // https://en.wikipedia.org/wiki/Algorithms_for_calculating_variance#Online_algorithm
        // https://www.johndcook.com/blog/2008/09/26/comparing-three-methods-of-computing-standard-deviation/
        this.variance = (1.0 - alpha) * (variance + diff * incr);
    }

}
