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
import com.expedia.adaptivealerting.anomdetect.forecast.interval.algo.multiplicative.MultiplicativeIntervalForecasterParams;
import com.expedia.adaptivealerting.anomdetect.forecast.point.algo.seasonalnaive.SeasonalNaivePointForecasterParams;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Setter;
import lombok.val;

@JsonIgnoreProperties(ignoreUnknown = true)
@Deprecated // Use ForecastingDetector with Seasonal Naive point forecaster
public class LegacySeasonalNaiveDetectorParams {

    // Setter-only to avoid test coverage gaps

    // Seasonal Naive params
    @Setter
    private int cycleLength;
    @Setter
    private int intervalLength;
    @Setter
    private double missingValuePlaceholder;

    // Multiplicative params
    @Setter
    private double weakMultiplier;
    @Setter
    private double strongMultiplier;

    public LegacySeasonalNaiveDetectorParams() {
        // These keep the default values DRY.
        initWithSeasonalNaiveDefaults();
        initWithMultiplicativeDefaults();
    }

    public SeasonalNaivePointForecasterParams toSeasonalNaiveParams() {
        return new SeasonalNaivePointForecasterParams()
                .setCycleLength(cycleLength)
                .setIntervalLength(intervalLength)
                .setMissingValuePlaceholder(missingValuePlaceholder);
    }

    public MultiplicativeIntervalForecasterParams toMultiplicativeParams() {
        return new MultiplicativeIntervalForecasterParams()
                .setWeakMultiplier(weakMultiplier)
                .setStrongMultiplier(strongMultiplier);
    }

    private void initWithSeasonalNaiveDefaults() {
        val params = new SeasonalNaivePointForecasterParams();
        this.cycleLength = params.getCycleLength();
        this.intervalLength = params.getIntervalLength();
        this.missingValuePlaceholder = params.getMissingValuePlaceholder();
    }

    private void initWithMultiplicativeDefaults() {
        val params = new ExponentialWelfordIntervalForecasterParams();
        this.weakMultiplier = params.getWeakSigmas();
        this.strongMultiplier = params.getStrongSigmas();
    }
}
