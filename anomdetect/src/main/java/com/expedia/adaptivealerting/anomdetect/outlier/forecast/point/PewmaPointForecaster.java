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
package com.expedia.adaptivealerting.anomdetect.outlier.forecast.point;

import com.expedia.metrics.MetricData;
import lombok.Data;
import lombok.Generated;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.val;

import static com.expedia.adaptivealerting.anomdetect.util.AssertUtil.isBetween;
import static com.expedia.adaptivealerting.anomdetect.util.AssertUtil.isTrue;
import static com.expedia.adaptivealerting.anomdetect.util.AssertUtil.notNull;

public class PewmaPointForecaster implements PointForecaster {

    @Getter
    @Generated // https://reflectoring.io/100-percent-test-coverage/
    private Params params;

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
        this(new Params());
    }

    public PewmaPointForecaster(Params params) {
        notNull(params, "params can't be null");
        this.params = params;

        // Init config
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

    @Data
    @Accessors(chain = true)
    public static class Params implements PointForecasterParams {

        // TODO Describe why we chose these defaults as appropriate.
        //  For example if the paper recommends them, we should say that.

        /**
         * Smoothing param.
         */
        private double alpha = 0.15;

        /**
         * Anomaly weighting param.
         */
        private double beta = 1.0;

        /**
         * Initial mean estimate.
         */
        private double initMeanEstimate = 0.0;

        /**
         * How many iterations to train for.
         */
        private int warmUpPeriod = 30;

        @Override
        public void validate() {
            isBetween(alpha, 0.0, 1.0, "Required: 0.0 <= alpha <= 1.0");
            isBetween(beta, 0.0, 1.0, "Required: 0.0 <= beta <= 1.0");
            isTrue(warmUpPeriod >= 0, "Required: warmUpPeriod >= 0");
        }
    }
}
