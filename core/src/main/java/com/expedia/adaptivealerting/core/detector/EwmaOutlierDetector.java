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

import static com.expedia.adaptivealerting.core.util.AssertUtil.isBetween;
import static java.lang.Math.abs;
import static java.lang.Math.sqrt;

/**
 * <p>
 * Anomaly detector based on the exponential weighted moving average. This is an online algorithm, meaning that it
 * updates the thresholds incrementally as new data comes in.
 * </p>
 * <p>
 * It takes a little while before the internal mean and variance estimates converge to something that makes sense. As a
 * rule of thumb, feed the detector 10 data points or so before using it for actual anomaly detection.
 * </p>
 * <p>
 * See https://en.wikipedia.org/wiki/Moving_average#Exponentially_weighted_moving_variance_and_standard_deviation.
 * </p>
 *
 * @author Willie Wheeler
 */
public class EwmaOutlierDetector implements OutlierDetector {
    
    /**
     * Smoothing param. Somewhat misnamed because higher values lead to less smoothing, but it's called the smoothing
     * smoothing parameter in the literature.
     */
    private double alpha;
    
    /**
     * Weak outlier threshold, in sigmas.
     */
    private double weakThreshold;
    
    /**
     * Strong outlier threshold, in sigmas.
     */
    private double strongThreshold;
    
    /**
     * Local mean estimate.
     */
    private double mean;
    
    /**
     * Local variance estimate.
     */
    private double variance;
    
    /**
     * Creates a new EWMA outlier detector with alpha = 0.5, weakThreshold = 2.0, strongThreshold = 3.0 and initValue =
     * 0.0.
     */
    public EwmaOutlierDetector() {
        this(0.5, 2.0, 3.0, 0.0);
    }
    
    /**
     * Creates a new EWMA outlier detector. Initial mean is given by initValue and initial variance is 0.
     *
     * @param alpha           Smoothing parameter.
     * @param weakThreshold   Weak outlier threshold, in sigmas.
     * @param strongThreshold Strong outlier threshold, in sigmas.
     * @param initValue       Initial observation, used to set the first mean estimate.
     */
    public EwmaOutlierDetector(double alpha, double weakThreshold, double strongThreshold, double initValue) {
        isBetween(alpha, 0.0, 1.0, "alpha must be in the range [0, 1]");
        
        this.alpha = alpha;
        this.weakThreshold = weakThreshold;
        this.strongThreshold = strongThreshold;
        this.mean = initValue;
        this.variance = 0.0;
    }
    
    public double getAlpha() {
        return alpha;
    }
    
    public double getWeakThreshold() {
        return weakThreshold;
    }
    
    public double getStrongThreshold() {
        return strongThreshold;
    }
    
    public double getMean() {
        return mean;
    }
    
    public double getVariance() {
        return variance;
    }
    
    @Override
    public OutlierLevel classify(MetricPoint metricPoint) {
        OutlierLevel level = null;
    
        final float value = metricPoint.value();
        final double dist = abs(value - mean);
        final double stdDev = sqrt(variance);
        final double largeThreshold = strongThreshold * stdDev;
        final double smallThreshold = weakThreshold * stdDev;
    
        if (dist > largeThreshold) {
            level = OutlierLevel.STRONG;
        } else if (dist > smallThreshold) {
            level = OutlierLevel.WEAK;
        } else {
            level = OutlierLevel.NORMAL;
        }
    
        updateEstimates(value);
    
        return level;
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
}
