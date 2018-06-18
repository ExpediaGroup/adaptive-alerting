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
import com.expedia.adaptivealerting.core.util.AssertUtil;
import com.expedia.www.haystack.commons.entities.MetricPoint;

import static com.expedia.adaptivealerting.core.anomaly.AnomalyLevel.*;
import static java.lang.Math.*;

/**
 * <p>
 * Anomaly detector based on the probabilistic exponential weighted moving average. This is an online algorithm, meaning
 * that it updates the thresholds incrementally as new data comes in, but is less influenced by outliers than EWMA.
 * </p>
 * <p>
 * It takes a little while before the internal mean and variance estimates converge to something that makes sense. As a
 * rule of thumb, feed the detector 30 data points or so before using it for actual anomaly detection.
 * </p>
 * <p>
 * See https://www.ll.mit.edu/mission/cybersec/publications/publication-files/full_papers/2012_08_05_Carter_IEEESSP_FP.pdf.
 * Implementation based off code from here https://aws.amazon.com/blogs/iot/anomaly-detection-using-aws-iot-and-aws-lambda/.
 *
 * </p>
 *
 * @author David Sutherland
 */
public class PewmaAnomalyDetector implements AnomalyDetector {
    static final int DEFAULT_TRAINING_LENGTH = 30;
    
    /**
     * Smoothing param.
     */
    private final double alpha0;
    
    /**
     * Outlier weighting param.
     */
    private final double beta;
    
    /**
     * How many iterations to train for.
     */
    private final double trainingLength;
    
    /**
     * Smoothing param.
     */
    private double trainingCount;
    
    /**
     * Internal state for PEWMA algorithm.
     */
    private double s1;
    
    /**
     * Internal state for PEWMA algorithm.
     */
    private double s2;
    
    /**
     * Strong outlier threshold, in sigmas.
     */
    private final double strongThresholdSigmas;
    
    /**
     * Weak outlier threshold, in sigmas.
     */
    private final double weakThresholdSigmas;
    
    /**
     * Local mean estimate.
     */
    private double mean;
    
    /**
     * Local standard deviation estimate.
     */
    private double stdDev;
    
    /**
     * Creates a new PEWMA outlier detector with initialAlpha = 0.15, beta = 1.0, weakThresholdSigmas = 3.0,
     * strongThresholdSigmas = 4.0 and initValue = 0.0.
     */
    public PewmaAnomalyDetector() {
        this(0.15, 1.0, 3.0, 4.0, 0.0);
    }
    
    // FIXME "initialAlpha" is a confusing name for this parameter as it suggests that the value evolves over time.
    // I understand that the intent is to handle reversing the alpha. I would suggest just using "alpha" as the param
    // name since that's what we expose to the client, and then choosing some other name internally (lambda or
    // reverseAlpha or alphaPrime or whatever). I think it's more important that EWMA and PEWMA expose use the same
    // name for the parameter in question than it is for this implementation to use the same name as what's in the
    // paper.
    //
    // If you think that's likely to be confusing, then another option would be to expose a parameter called "lambda"
    // on both EWMA and PEWMA, and then use alpha here as per the paper.
    //
    // I don't have a strong preference between those two. [WLW]
    
    /**
     * Creates a new PEWMA outlier detector. Initial mean is given by initValue and initial standard deviation is 0.
     *
     * @param initialAlpha          Smoothing parameter.
     * @param beta                  Outlier weighting parameter.
     * @param weakThresholdSigmas   Weak outlier threshold, in sigmas.
     * @param strongThresholdSigmas Strong outlier threshold, in sigmas.
     * @param initValue             Initial observation, used to set the first mean estimate.
     */
    public PewmaAnomalyDetector(
            double initialAlpha,
            double beta,
            double weakThresholdSigmas,
            double strongThresholdSigmas,
            double initValue
    ) {
        this(initialAlpha, beta, DEFAULT_TRAINING_LENGTH, weakThresholdSigmas, strongThresholdSigmas, initValue);
    }
    
    /**
     * Creates a new PEWMA outlier detector. Initial mean is given by initValue and initial standard deviation is 0.
     *
     * @param initialAlpha          Smoothing parameter.
     * @param beta                  Outlier weighting parameter.
     * @param trainingLength        How many iterations to train for.
     * @param weakThresholdSigmas   Weak outlier threshold, in sigmas.
     * @param strongThresholdSigmas Strong outlier threshold, in sigmas.
     * @param initValue             Initial observation, used to set the first mean estimate.
     */
    public PewmaAnomalyDetector(
            double initialAlpha,
            double beta,
            int trainingLength,
            double weakThresholdSigmas,
            double strongThresholdSigmas,
            double initValue
    ) {
        AssertUtil.isBetween(initialAlpha, 0.0, 1.0, "initialAlpha must be in the range [0, 1]");
        
        this.alpha0 = 1 - initialAlpha;  // To standardise with the EWMA implementation we use the complement value.
        this.beta = beta;
        this.trainingLength = trainingLength;
        this.trainingCount = 1;
        this.weakThresholdSigmas = weakThresholdSigmas;
        this.strongThresholdSigmas = strongThresholdSigmas;
        this.s1 = initValue;
        this.s2 = initValue * initValue;
        updateMeanAndStdDev();
    }
    
    private void updateMeanAndStdDev() {
        this.mean = this.s1;
        this.stdDev = sqrt(this.s2 - this.s1 * this.s1);
    }
    
    @Override
    public AnomalyResult classify(MetricPoint metricPoint) {
        AssertUtil.notNull(metricPoint, "metricPoint can't be null");
    
        final double observed = metricPoint.value();
        final double dist = abs(observed - mean);
        final double weakThreshold = weakThresholdSigmas * stdDev;
        final double strongThreshold = strongThresholdSigmas * stdDev;
    
        AnomalyLevel anomalyLevel = NORMAL;
        if (dist > strongThreshold) {
            anomalyLevel = STRONG;
        } else if (dist > weakThreshold) {
            anomalyLevel = WEAK;
        }
    
        final AnomalyResult result = new AnomalyResult(this.getClass());
        result.setEpochSecond(metricPoint.epochTimeInSeconds());
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
        double zt = 0;
        if (this.stdDev != 0) {
            zt = (value - this.mean) / this.stdDev;
        }
        double pt = (1 / sqrt(2 * Math.PI)) * exp(-0.5 * zt * zt);
        double alpha = calculateAlpha(pt);
        
        this.s1 = alpha * this.s1 + (1 - alpha) * value;
        this.s2 = alpha * this.s2 + (1 - alpha) * value * value;
        
        updateMeanAndStdDev();
    }
    
    private double calculateAlpha(double pt) {
        if (this.trainingCount < this.trainingLength) {
            this.trainingCount++;
            return 1 - 1.0 / this.trainingCount;
        }
        return (1 - this.beta * pt) * this.alpha0;
    }
    
    public double getMean() {
        return mean;
    }
    
    public double getStdDev() {
        return stdDev;
    }
}
