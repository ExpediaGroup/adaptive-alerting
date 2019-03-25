/*
 * Copyright 2018-2019 Expedia Group, Inc.
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
package com.expedia.adaptivealerting.anomdetect.forecast.point;

import com.expedia.adaptivealerting.anomdetect.comp.AnomalyClassifier;
import com.expedia.adaptivealerting.anomdetect.detector.AbstractDetector;
import com.expedia.adaptivealerting.core.anomaly.AnomalyResult;
import com.expedia.adaptivealerting.core.anomaly.AnomalyThresholds;
import com.expedia.metrics.MetricData;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.val;

import static com.expedia.adaptivealerting.core.util.AssertUtil.notNull;

// TODO Return MODEL_WARMUP anomaly level if the model is still warming up. [WLW]

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
 * </p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
public final class PewmaDetector extends AbstractDetector<PewmaParams> {

    /**
     * Adjusted alpha, to match the way alpha is used in the paper that describes the algorithm.
     */
    private double adjAlpha;

    /**
     * Number of data points seen so far.
     */
    private int trainingCount = 1;

    /**
     * Internal state for PEWMA algorithm.
     */
    private double s1;

    /**
     * Internal state for PEWMA algorithm.
     */
    private double s2;

    /**
     * Local mean estimate.
     */
    private double mean;

    /**
     * Local standard deviation estimate.
     */
    private double stdDev;

    public PewmaDetector() {
        super(PewmaParams.class);
    }

    @Override
    protected void initState(PewmaParams params) {
        this.adjAlpha = 1.0 - params.getAlpha();
        this.s1 = params.getInitMeanEstimate();
        this.s2 = params.getInitMeanEstimate() * params.getInitMeanEstimate();
        updateMeanAndStdDev();
    }

    @Override
    public AnomalyResult classify(MetricData metricData) {
        notNull(metricData, "metricData can't be null");

        val params = getParams();

        val observed = metricData.getValue();
        val weakDelta = params.getWeakSigmas() * stdDev;
        val strongDelta = params.getStrongSigmas() * stdDev;

        val thresholds = new AnomalyThresholds(
                mean + strongDelta,
                mean + weakDelta,
                mean - weakDelta,
                mean - strongDelta);

        updateEstimates(observed);

        val level = new AnomalyClassifier(getAnomalyType()).classify(thresholds, observed);

        val result = new AnomalyResult(level);
        result.setPredicted(mean);
        result.setThresholds(thresholds);
        return result;
    }

    private void updateEstimates(double value) {
        double zt = 0;
        if (this.stdDev != 0.0) {
            zt = (value - this.mean) / this.stdDev;
        }
        double pt = (1.0 / Math.sqrt(2.0 * Math.PI)) * Math.exp(-0.5 * zt * zt);
        double alpha = calculateAlpha(pt);

        this.s1 = alpha * this.s1 + (1.0 - alpha) * value;
        this.s2 = alpha * this.s2 + (1.0 - alpha) * value * value;

        updateMeanAndStdDev();
    }

    private void updateMeanAndStdDev() {
        this.mean = this.s1;
        this.stdDev = Math.sqrt(this.s2 - this.s1 * this.s1);
    }

    private double calculateAlpha(double pt) {
        val params = getParams();
        if (this.trainingCount < params.getWarmUpPeriod()) {
            this.trainingCount++;
            return 1.0 - 1.0 / this.trainingCount;
        }
        return (1.0 - params.getBeta() * pt) * this.adjAlpha;
    }
}
