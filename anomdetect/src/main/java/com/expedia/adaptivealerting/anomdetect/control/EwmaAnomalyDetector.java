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
import com.expedia.adaptivealerting.core.anomaly.AnomalyThresholds;
import com.expedia.metrics.MetricData;
import lombok.Getter;
import lombok.ToString;

import java.util.UUID;

import static com.expedia.adaptivealerting.anomdetect.NSigmasClassifier.DEFAULT_STRONG_SIGMAS;
import static com.expedia.adaptivealerting.anomdetect.NSigmasClassifier.DEFAULT_WEAK_SIGMAS;
import static com.expedia.adaptivealerting.core.util.AssertUtil.isBetween;
import static com.expedia.adaptivealerting.core.util.AssertUtil.notNull;
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
@ToString
public final class EwmaAnomalyDetector extends AbstractAnomalyDetector {
    
    /**
     * Smoothing param. Somewhat misnamed because higher values lead to less smoothing, but it's called the smoothing
     * parameter in the literature.
     */
    @Getter
    private double alpha;
    
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
     * Local mean estimate.
     */
    @Getter
    private double mean;
    
    /**
     * Local variance estimate.
     */
    @Getter
    private double variance;
    
    /**
     * Creates a new EWMA outlier detector with alpha = 0.15, weakSigmas = 3.0, strongSigmas = 4.0 and
     * initValue = 0.0.
     */
    public EwmaAnomalyDetector(UUID uuid) {
        this(uuid, 0.15, DEFAULT_STRONG_SIGMAS, DEFAULT_WEAK_SIGMAS, 0.0);
    }
    
    /**
     * Creates a new EWMA anomaly detector. Initial mean is given by initValue and initial variance is 0.
     *
     * @param uuid         Detector UUID.
     * @param alpha        Smoothing parameter.
     * @param strongSigmas Strong outlier threshold, in sigmas.
     * @param weakSigmas   Weak outlier threshold, in sigmas.
     * @param initValue    Initial observation, used to set the first mean estimate.
     */
    public EwmaAnomalyDetector(
            UUID uuid,
            double alpha,
            double strongSigmas,
            double weakSigmas,
            double initValue) {
        
        super(uuid);
        
        isBetween(alpha, 0.0, 1.0, "alpha must be in the range [0, 1]");
        
        this.alpha = alpha;
        this.strongSigmas = strongSigmas;
        this.weakSigmas = weakSigmas;
        this.mean = initValue;
        this.variance = 0.0;
    }
    
    @Override
    public AnomalyResult classify(MetricData metricData) {
        notNull(metricData, "metricData can't be null");
        
        final double observed = metricData.getValue();
        final double stdDev = sqrt(variance);
        final double strongDelta = strongSigmas * stdDev;
        final double weakDelta = weakSigmas * stdDev;
        
        final AnomalyThresholds thresholds = new AnomalyThresholds(
                mean + strongDelta,
                mean + weakDelta,
                mean - strongDelta,
                mean - weakDelta);
        
        updateEstimates(observed);
        
        final AnomalyLevel level = thresholds.classify(observed);
        
        final AnomalyResult result = anomalyResult(metricData, level);
        result.setPredicted(mean);
        result.setThresholds(thresholds);
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
}
