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
package com.expedia.adaptivealerting.anomdetect.forecast.interval.algo;

import com.expedia.adaptivealerting.anomdetect.forecast.interval.IntervalForecast;
import com.expedia.adaptivealerting.anomdetect.forecast.interval.IntervalForecaster;
import com.expedia.metrics.MetricData;
import lombok.Getter;
import lombok.val;

import static com.expedia.adaptivealerting.anomdetect.util.AssertUtil.notNull;

/**
 * Interval forecaster based on a modified version of Welford's algorithm for computing sample variance in an online
 * fashion. The modification involves using exponential weighting to control the algorithm's emphasis on recent vs
 * historical samples.
 *
 * <ul>
 * <li>https://en.wikipedia.org/wiki/Algorithms_for_calculating_variance#Online_algorithm</li>
 * <li>https://en.wikipedia.org/wiki/Moving_average#Exponentially_weighted_moving_variance_and_standard_deviation</li>
 * <li>http://people.ds.cam.ac.uk/fanf2/hermes/doc/antiforgery/stats.pdf</li>
 * <li>https://www.johndcook.com/blog/standard_deviation/</li>
 * <li>https://www.johndcook.com/blog/2008/09/26/comparing-three-methods-of-computing-standard-deviation/</li>
 * </ul>
 */
public class ExponentialWelfordIntervalForecaster implements IntervalForecaster {

    @Getter
    private ExponentialWelfordIntervalForecasterParams params;

    @Getter
    private double variance;

    public ExponentialWelfordIntervalForecaster() {
        this(new ExponentialWelfordIntervalForecasterParams());
    }

    public ExponentialWelfordIntervalForecaster(ExponentialWelfordIntervalForecasterParams params) {
        notNull(params, "params can't be null");
        this.params = params;
        this.variance = params.getInitVarianceEstimate();
    }

    @Override
    public IntervalForecast forecast(MetricData metricData, double pointForecast) {

        // https://en.wikipedia.org/wiki/Moving_average#Exponentially_weighted_moving_variance_and_standard_deviation
        // http://people.ds.cam.ac.uk/fanf2/hermes/doc/antiforgery/stats.pdf
        val observed = metricData.getValue();
        val residual = observed - pointForecast;
        val incr = params.getAlpha() * residual;

        // FIXME I believe this belongs here... [WLW]
        this.variance = (1.0 - params.getAlpha()) * (this.variance + residual * incr);

        val stdev = Math.sqrt(variance);
        val weakWidth = params.getWeakSigmas() * stdev;
        val strongWidth = params.getStrongSigmas() * stdev;

        // FIXME ...but this is where it is in the legacy code (and where the unit tests expect it). [WLW]
//        this.variance = (1.0 - params.getAlpha()) * (this.variance + residual * incr);

        return new IntervalForecast(
                pointForecast + strongWidth,
                pointForecast + weakWidth,
                pointForecast - weakWidth,
                pointForecast - strongWidth);
    }

}
