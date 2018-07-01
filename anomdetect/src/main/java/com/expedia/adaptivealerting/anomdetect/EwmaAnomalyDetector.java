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

import com.expedia.adaptivealerting.core.anomaly.AnomalyLevel;
import com.expedia.adaptivealerting.core.anomaly.AnomalyResult;
import com.expedia.adaptivealerting.core.data.Mpoint;
import com.expedia.adaptivealerting.core.util.AssertUtil;
import com.expedia.adaptivealerting.core.util.MetricPointUtil;
import com.expedia.www.haystack.commons.entities.MetricPoint;

import static com.expedia.adaptivealerting.anomdetect.NSigmasClassifier.DEFAULT_STRONG_SIGMAS;
import static com.expedia.adaptivealerting.anomdetect.NSigmasClassifier.DEFAULT_WEAK_SIGMAS;
import static com.expedia.adaptivealerting.core.anomaly.AnomalyLevel.*;
import static java.lang.Math.abs;
import static java.lang.Math.sqrt;

/**
 * <p>
 * Anomaly detector based on the exponential weighted moving average (EWMA) chart, a type of control chart used in
 * statistical quality control. This is an online algorithm, meaning that it updates the thresholds incrementally as new
 * data comes in.
 * </p>
 * <p>
 * It takes a little while before the internal mean and variance estimates converge to something that makes sense. As a
 * rule of thumb, feed the detector 10 data points or so before using it for actual anomaly detection.
 * </p>
 *
 * @author Willie Wheeler
 * @see <a href="https://en.wikipedia.org/wiki/EWMA_chart">EWMA chart</a>
 * @see <a href="https://en.wikipedia.org/wiki/Moving_average#Exponentially_weighted_moving_variance_and_standard_deviation">Exponentially weighted moving average and standard deviation</a>
 * @see <a href="https://www.itl.nist.gov/div898/handbook/pmc/section3/pmc324.htm">EWMA Control Charts</a>
 */
public class EwmaAnomalyDetector implements AnomalyDetector {
    
    /**
     * Smoothing param. Somewhat misnamed because higher values lead to less smoothing, but it's called the smoothing
     * parameter in the literature.
     */
    private double alpha;
    
    /**
     * Strong outlier threshold, in sigmas.
     */
    private double strongThresholdSigmas;
    
    /**
     * Weak outlier threshold, in sigmas.
     */
    private double weakThresholdSigmas;
    
    /**
     * Local mean estimate.
     */
    private double mean;
    
    /**
     * Local variance estimate.
     */
    private double variance;
    
    /**
     * Creates a new EWMA outlier detector with alpha = 0.15, weakThresholdSigmas = 3.0, strongThresholdSigmas = 4.0 and
     * initValue = 0.0.
     */
    public EwmaAnomalyDetector() {
        this(0.15, DEFAULT_WEAK_SIGMAS, DEFAULT_STRONG_SIGMAS, 0.0);
    }
    
    /**
     * Creates a new EWMA outlier detector. Initial mean is given by initValue and initial variance is 0.
     *
     * @param alpha                 Smoothing parameter.
     * @param weakThresholdSigmas   Weak outlier threshold, in sigmas.
     * @param strongThresholdSigmas Strong outlier threshold, in sigmas.
     * @param initValue             Initial observation, used to set the first mean estimate.
     */
    public EwmaAnomalyDetector(
            double alpha,
            double weakThresholdSigmas,
            double strongThresholdSigmas,
            double initValue) {
        
        AssertUtil.isBetween(alpha, 0.0, 1.0, "alpha must be in the range [0, 1]");
        
        this.alpha = alpha;
        this.weakThresholdSigmas = weakThresholdSigmas;
        this.strongThresholdSigmas = strongThresholdSigmas;
        this.mean = initValue;
        this.variance = 0.0;
    }
    
    public double getAlpha() {
        return alpha;
    }
    
    public double getWeakThresholdSigmas() {
        return weakThresholdSigmas;
    }
    
    public double getStrongThresholdSigmas() {
        return strongThresholdSigmas;
    }
    
    public double getMean() {
        return mean;
    }
    
    public double getVariance() {
        return variance;
    }
    
    @Override
    public AnomalyResult classify(MetricPoint metricPoint) {
        AssertUtil.notNull(metricPoint, "metricPoint can't be null");
        
        final double observed = metricPoint.value();
        final double dist = abs(observed - mean);
        final double stdDev = sqrt(variance);
        final double weakThreshold = weakThresholdSigmas * stdDev;
        final double strongThreshold = strongThresholdSigmas * stdDev;
        
        AnomalyLevel anomalyLevel = NORMAL;
        if (dist > strongThreshold) {
            anomalyLevel = STRONG;
        } else if (dist > weakThreshold) {
            anomalyLevel = WEAK;
        }
        
        final Mpoint mpoint = MetricPointUtil.toMpoint(metricPoint);
        final AnomalyResult result = new AnomalyResult();
        result.setMetric(mpoint.getMetric());
        result.setDetectorId(this.getId());
        result.setEpochSecond(mpoint.getEpochTimeInSeconds());
        result.setObserved(observed);
        result.setPredicted(mean);
        result.setWeakThresholdUpper(mean + weakThreshold);
        result.setWeakThresholdLower(mean - weakThreshold);
        result.setStrongThresholdUpper(mean + strongThreshold);
        result.setStrongThresholdLower(mean - strongThreshold);
        result.setAnomalyScore(dist);
        result.setAnomalyLevel(anomalyLevel);
        
        updateEstimates(observed);
        
        return result;
    }
    
    private void updateEstimates(double value) {
        
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
    
    @Override
    public String toString() {
        return "PewmaAnomalyDetector{" +
                "alpha=" + alpha +
                ", weakThresholdSigmas=" + weakThresholdSigmas +
                ", strongThresholdSigmas=" + strongThresholdSigmas +
                '}';
    }
}
