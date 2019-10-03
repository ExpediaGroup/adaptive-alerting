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

import com.expedia.adaptivealerting.anomdetect.forecast.interval.algo.expwelford.ExponentialWelfordIntervalForecasterParams;
import com.expedia.adaptivealerting.anomdetect.forecast.point.algo.holtwinters.HoltWintersPointForecasterParams;
import com.expedia.adaptivealerting.anomdetect.forecast.point.algo.holtwinters.HoltWintersSeasonalityType;
import com.expedia.adaptivealerting.anomdetect.forecast.point.algo.holtwinters.HoltWintersTrainingMethod;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Setter;
import lombok.val;

@JsonIgnoreProperties(ignoreUnknown = true)
@Deprecated // Use ForecastingDetector with Holt-Winters point forecaster
public class LegacyHoltWintersDetectorParams {

    // Setter-only to avoid test coverage gaps

    // Holt-Winters params
    @Setter
    private int frequency;
    @Setter
    private double alpha;
    @Setter
    private double beta;
    @Setter
    private double gamma;
    @Setter
    private HoltWintersSeasonalityType seasonalityType;
    @Setter
    private HoltWintersTrainingMethod initTrainingMethod;
    @Setter
    private double initLevelEstimate;
    @Setter
    private double initBaseEstimate;
    @Setter
    private double[] initSeasonalEstimates;
    @Setter
    private int warmUpPeriod;

    // Welford params
    @Setter
    private double weakSigmas;
    @Setter
    private double strongSigmas;

    public LegacyHoltWintersDetectorParams() {
        // These keep the default values DRY.
        initWithHoltWintersDefaults();
        initWithWelfordDefaults();
    }

    public HoltWintersPointForecasterParams toHoltWintersParams() {
        return new HoltWintersPointForecasterParams()
                .setFrequency(frequency)
                .setAlpha(alpha)
                .setBeta(beta)
                .setGamma(gamma)
                .setSeasonalityType(seasonalityType)
                .setInitTrainingMethod(initTrainingMethod)
                .setInitLevelEstimate(initLevelEstimate)
                .setInitBaseEstimate(initBaseEstimate)
                .setInitSeasonalEstimates(initSeasonalEstimates)
                .setWarmUpPeriod(warmUpPeriod);
    }

    public ExponentialWelfordIntervalForecasterParams toWelfordParams() {
        return new ExponentialWelfordIntervalForecasterParams()
                .setWeakSigmas(weakSigmas)
                .setStrongSigmas(strongSigmas);
    }

    private void initWithHoltWintersDefaults() {
        val params = new HoltWintersPointForecasterParams();
        this.frequency = params.getFrequency();
        this.alpha = params.getAlpha();
        this.beta = params.getBeta();
        this.gamma = params.getGamma();
        this.seasonalityType = params.getSeasonalityType();
        this.initTrainingMethod = params.getInitTrainingMethod();
        this.initLevelEstimate = params.getInitLevelEstimate();
        this.initBaseEstimate = params.getInitBaseEstimate();
        this.initSeasonalEstimates = params.getInitSeasonalEstimates();
        this.warmUpPeriod = params.getWarmUpPeriod();
    }

    private void initWithWelfordDefaults() {
        val params = new ExponentialWelfordIntervalForecasterParams();
        this.weakSigmas = params.getWeakSigmas();
        this.strongSigmas = params.getStrongSigmas();
    }
}
