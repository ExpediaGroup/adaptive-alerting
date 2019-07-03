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
package com.expedia.adaptivealerting.anomdetect.source.factory;

import com.expedia.adaptivealerting.anomdetect.forecast.interval.algo.ExponentialWelfordIntervalForecasterParams;
import com.expedia.adaptivealerting.anomdetect.forecast.point.algo.PewmaPointForecasterParams;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Setter;
import lombok.val;

@JsonIgnoreProperties(ignoreUnknown = true)
@Deprecated // Use ForecastingDetector with EWMA point forecaster
public class LegacyPewmaParams {

    // Setter-only to avoid test coverage gaps

    // PEWMA params
    @Setter
    private double alpha;
    @Setter
    private double beta;
    @Setter
    private double initMeanEstimate;
    @Setter
    private int warmUpPeriod;

    // Welford params
    @Setter
    private double weakSigmas;
    @Setter
    private double strongSigmas;

    public LegacyPewmaParams() {
        // These keep the default values DRY.
        initWithPewmaDefaults();
        initWithWelfordDefaults();
    }

    public PewmaPointForecasterParams toPewmaParams() {
        return new PewmaPointForecasterParams()
                .setAlpha(alpha)
                .setBeta(beta)
                .setInitMeanEstimate(initMeanEstimate)
                .setWarmUpPeriod(warmUpPeriod);
    }

    public ExponentialWelfordIntervalForecasterParams toWelfordParams() {
        return new ExponentialWelfordIntervalForecasterParams()
                .setWeakSigmas(weakSigmas)
                .setStrongSigmas(strongSigmas);
    }

    private void initWithPewmaDefaults() {
        val params = new PewmaPointForecasterParams();
        this.alpha = params.getAlpha();
        this.beta = params.getBeta();
        this.initMeanEstimate = params.getInitMeanEstimate();
        this.warmUpPeriod = params.getWarmUpPeriod();
    }

    private void initWithWelfordDefaults() {
        val params = new ExponentialWelfordIntervalForecasterParams();
        this.weakSigmas = params.getWeakSigmas();
        this.strongSigmas = params.getStrongSigmas();
    }
}
