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
package com.expedia.adaptivealerting.anomdetect.comp.legacy;

import com.expedia.adaptivealerting.anomdetect.comp.AnomalyClassifier;
import com.expedia.adaptivealerting.anomdetect.detector.AbstractDetector;
import com.expedia.adaptivealerting.anomdetect.forecast.interval.ExponentialWelfordIntervalForecaster;
import com.expedia.adaptivealerting.anomdetect.forecast.interval.IntervalForecast;
import com.expedia.adaptivealerting.core.anomaly.AnomalyResult;
import com.expedia.adaptivealerting.core.anomaly.AnomalyThresholds;
import com.expedia.metrics.MetricData;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.val;

import static com.expedia.adaptivealerting.core.util.AssertUtil.notNull;

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
@Data
@EqualsAndHashCode(callSuper = true)
@Deprecated
public final class EwmaDetector extends AbstractDetector<EwmaParams> {

    // TODO Temporarily using an internal interval forecaster. Will externalize this shortly. [WLW]
    private ExponentialWelfordIntervalForecaster intervalForecaster;

    /**
     * Mean estimate.
     */
    @Getter
    private double mean = 0.0;

    public EwmaDetector() {
        super(EwmaParams.class);
    }

    @Override
    protected void initComponents(EwmaParams params) {
        val welfordParams = new ExponentialWelfordIntervalForecaster.Params()
                .setAlpha(params.getAlpha())
                .setInitVarianceEstimate(0.0)
                .setWeakSigmas(params.getWeakSigmas())
                .setStrongSigmas(params.getStrongSigmas());
        this.intervalForecaster = new ExponentialWelfordIntervalForecaster(welfordParams);
    }

    @Override
    protected void initState(EwmaParams params) {
        this.mean = params.getInitMeanEstimate();
    }

    public double getVariance() {
        return intervalForecaster.getVariance();
    }

    @Override
    public AnomalyResult classify(MetricData metricData) {
        notNull(metricData, "metricData can't be null");

        val observed = metricData.getValue();
        val intervalForecast = intervalForecaster.forecast(metricData, mean);
        val thresholds = toAnomalyThresholds(intervalForecast);
        val level = new AnomalyClassifier(getAnomalyType()).classify(thresholds, observed);

        updateEstimates(observed);

        val result = new AnomalyResult(level);
        result.setPredicted(this.mean);
        result.setThresholds(thresholds);
        return result;
    }

    private AnomalyThresholds toAnomalyThresholds(IntervalForecast intervalForecast) {
        return new AnomalyThresholds(
                intervalForecast.getUpperStrong(),
                intervalForecast.getUpperWeak(),
                intervalForecast.getLowerWeak(),
                intervalForecast.getLowerStrong());
    }

    private void updateEstimates(double value) {

        // https://en.wikipedia.org/wiki/Moving_average#Exponentially_weighted_moving_variance_and_standard_deviation
        // http://people.ds.cam.ac.uk/fanf2/hermes/doc/antiforgery/stats.pdf
        val diff = value - this.mean;
        val incr = getParams().getAlpha() * diff;
        this.mean += incr;
    }
}
