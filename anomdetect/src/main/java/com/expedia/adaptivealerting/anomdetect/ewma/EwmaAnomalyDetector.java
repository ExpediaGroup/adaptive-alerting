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
package com.expedia.adaptivealerting.anomdetect.ewma;

import com.expedia.adaptivealerting.anomdetect.AbstractAnomalyDetector;
import com.expedia.adaptivealerting.core.anomaly.AnomalyResult;
import com.expedia.adaptivealerting.core.anomaly.AnomalyThresholds;
import com.expedia.metrics.MetricData;
import lombok.Getter;
import lombok.val;

import static com.expedia.adaptivealerting.core.util.AssertUtil.notNull;
import static java.lang.Math.sqrt;

// TODO Add model warmup param and anomaly level. See e.g. CUSUM, Individuals, PEWMA. [WLW]

/**
 * <p>
 * Anomaly detector based on the exponential weighted moving average (EWMA) chart, a type of control chart used in
 * statistical quality control. This is an online algorithm, meaning that it updates the thresholds incrementally as new
 * data comes in.
 * </p>
 * <p>
 * EWMA is also called "Single Exponential Smoothing", "Simple Exponential Smoothing" or "Basic Exponential Smoothing".
 * </p>
 * <p>
 * It takes a little while before the internal mean and variance estimates converge to something that makes sense. As a
 * rule of thumb, feed the detector 10 data points or so before using it for actual anomaly detection.
 * </p>
 *
 * @see <a href="https://en.wikipedia.org/wiki/EWMA_chart">EWMA chart</a>
 * @see <a href="https://en.wikipedia.org/wiki/Moving_average#Exponentially_weighted_moving_variance_and_standard_deviation">Exponentially weighted moving average and standard deviation</a>
 * @see <a href="https://www.itl.nist.gov/div898/handbook/pmc/section3/pmc324.htm">EWMA Control Charts</a>
 */
public final class EwmaAnomalyDetector extends AbstractAnomalyDetector<EwmaParams> {

    /**
     * Mean estimate.
     */
    @Getter
    private double mean = 0.0;

    /**
     * Variance estimate.
     */
    @Getter
    private double variance = 0.0;

    public EwmaAnomalyDetector() {
        super(EwmaParams.class);
    }

    @Override
    protected void initState(EwmaParams params) {
        this.mean = params.getInitMeanEstimate();
    }

    @Override
    public AnomalyResult classify(MetricData metricData) {
        notNull(metricData, "metricData can't be null");

        val params = getParams();

        val observed = metricData.getValue();
        val stdDev = sqrt(this.variance);
        val weakDelta = params.getWeakSigmas() * stdDev;
        val strongDelta = params.getStrongSigmas() * stdDev;

        final AnomalyThresholds thresholds = new AnomalyThresholds(
                this.mean + strongDelta,
                this.mean + weakDelta,
                this.mean - weakDelta,
                this.mean - strongDelta
        );

        updateEstimates(observed);

        val level = thresholds.classify(observed);

        val result = new AnomalyResult(level);
        result.setPredicted(this.mean);
        result.setThresholds(thresholds);
        return result;
    }

    private void updateEstimates(double value) {
        val params = getParams();

        // https://en.wikipedia.org/wiki/Moving_average#Exponentially_weighted_moving_variance_and_standard_deviation
        // http://people.ds.cam.ac.uk/fanf2/hermes/doc/antiforgery/stats.pdf
        val diff = value - this.mean;
        val incr = params.getAlpha() * diff;
        this.mean += incr;

        // Welford's algorithm for computing the variance online
        // https://en.wikipedia.org/wiki/Algorithms_for_calculating_variance#Online_algorithm
        // https://www.johndcook.com/blog/2008/09/26/comparing-three-methods-of-computing-standard-deviation/
        this.variance = (1.0 - params.getAlpha()) * (this.variance + diff * incr);
    }
}
