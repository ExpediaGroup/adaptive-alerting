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
package com.expedia.adaptivealerting.anomdetect.detect.outlier.algo.forecasting;

import com.expedia.adaptivealerting.anomdetect.forecast.point.algo.ewma.EwmaPointForecasterParams;
import com.expedia.adaptivealerting.anomdetect.forecast.interval.algo.expwelford.ExponentialWelfordIntervalForecasterParams;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Setter;
import lombok.val;

@JsonIgnoreProperties(ignoreUnknown = true)
@Deprecated // Use ForecastingDetector with EWMA point forecaster
public class LegacyEwmaDetectorParams {

    // Setter-only to avoid test coverage gaps

    // EWMA params
    @Setter
    private double alpha;
    @Setter
    private double initMeanEstimate;

    // Welford params
    @Setter
    private double weakSigmas;
    @Setter
    private double strongSigmas;

    public LegacyEwmaDetectorParams() {
        // These keep the default values DRY.
        initWithEwmaDefaults();
        initWithWelfordDefaults();
    }

    public EwmaPointForecasterParams toEwmaParams() {
        return new EwmaPointForecasterParams()
                .setAlpha(alpha)
                .setInitMeanEstimate(initMeanEstimate);
    }

    public ExponentialWelfordIntervalForecasterParams toWelfordParams() {
        return new ExponentialWelfordIntervalForecasterParams()
                .setWeakSigmas(weakSigmas)
                .setStrongSigmas(strongSigmas);
    }

    private void initWithEwmaDefaults() {
        val params = new EwmaPointForecasterParams();
        this.alpha = params.getAlpha();
        this.initMeanEstimate = params.getInitMeanEstimate();
    }

    private void initWithWelfordDefaults() {
        val params = new ExponentialWelfordIntervalForecasterParams();
        this.weakSigmas = params.getWeakSigmas();
        this.strongSigmas = params.getStrongSigmas();
    }
}
