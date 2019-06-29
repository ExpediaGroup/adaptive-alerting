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
package com.expedia.adaptivealerting.anomdetect.forecast.algo;

import com.expedia.adaptivealerting.anomdetect.forecast.PointForecast;
import com.expedia.adaptivealerting.anomdetect.forecast.PointForecaster;
import com.expedia.metrics.MetricData;
import lombok.Generated;
import lombok.Getter;
import lombok.val;

import static com.expedia.adaptivealerting.anomdetect.util.AssertUtil.notNull;

public class PewmaPointForecaster implements PointForecaster {

    @Getter
    @Generated // https://reflectoring.io/100-percent-test-coverage/
    private PewmaPointForecasterParams params;

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
    @Getter
    private double mean;

    /**
     * Local standard deviation estimate.
     */
    @Getter
    private double stdDev;

    public PewmaPointForecaster() {
        this(new PewmaPointForecasterParams());
    }

    public PewmaPointForecaster(PewmaPointForecasterParams params) {
        notNull(params, "params can't be null");
        this.params = params;

        // Init detector-docs
        this.adjAlpha = 1.0 - params.getAlpha();

        // Init state
        this.s1 = params.getInitMeanEstimate();
        this.s2 = s1 * s1;
        updateMeanAndStdDev();
    }

    @Override
    public PointForecast forecast(MetricData metricData) {
        notNull(metricData, "metricData can't be null");
        val observed = metricData.getValue();
        updateEstimates(observed);

        // TODO Handle warmup
        return new PointForecast(mean, false);
    }

    private void updateMeanAndStdDev() {
        this.mean = this.s1;
        this.stdDev = Math.sqrt(this.s2 - this.s1 * this.s1);
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

    private double calculateAlpha(double pt) {
        if (this.trainingCount < params.getWarmUpPeriod()) {
            this.trainingCount++;
            return 1.0 - 1.0 / this.trainingCount;
        }
        return (1.0 - params.getBeta() * pt) * this.adjAlpha;
    }

}
