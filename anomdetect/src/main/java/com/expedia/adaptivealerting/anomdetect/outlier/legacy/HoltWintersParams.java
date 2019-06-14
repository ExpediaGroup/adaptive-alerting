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
package com.expedia.adaptivealerting.anomdetect.outlier.legacy;

import com.expedia.adaptivealerting.anomdetect.outlier.forecast.interval.ExponentialWelfordIntervalForecaster;
import com.expedia.adaptivealerting.anomdetect.outlier.forecast.point.HoltWintersForecaster;
import com.expedia.adaptivealerting.anomdetect.outlier.forecast.point.holtwinters.HoltWintersSeasonalEstimatesValidator;
import com.expedia.adaptivealerting.anomdetect.outlier.forecast.point.holtwinters.HoltWintersTrainingMethod;
import com.expedia.adaptivealerting.anomdetect.outlier.forecast.point.holtwinters.SeasonalityType;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;

import static com.expedia.adaptivealerting.anomdetect.util.AssertUtil.isTrue;
import static com.expedia.adaptivealerting.anomdetect.util.AssertUtil.notNull;

/**
 * @see <a href="https://otexts.org/fpp2/holt-winters.html">Holt-Winters' Seasonal Method</a>
 * and <a href="https://robjhyndman.com/hyndsight/seasonal-periods/">https://robjhyndman.com/hyndsight/seasonal-periods/</a> for naming conventions
 * (e.g. usage of "frequency" and "cycle").
 */
@Data
@Accessors(chain = true)
@Slf4j
@Deprecated
public final class HoltWintersParams {

    /**
     * A default alpha value for the exponential Welford interval forecaster.
     */
    private static final double DEFAULT_EXP_WELFORD_ALPHA = 0.15;

    /**
     * SeasonalityType parameter used to determine which Seasonality method (Multiplicative or Additive) to use.
     */
    private SeasonalityType seasonalityType = SeasonalityType.MULTIPLICATIVE;

    /**
     * Frequency parameter representing periodicity of the data.
     * E.g. 24 = data is provided in hourly samples and seasons are represented as single days.
     * E.g.  7 = data is provided in daily samples and seasons are represented as single weeks.
     * E.g. 12 = data is provided in monthly samples and seasons are represented as single years.
     * E.g.  4 = data is provided in quarterly samples and seasons are represented as single years.
     */
    private int frequency = 0;

    /**
     * Alpha smoothing parameter used for "level" calculation.
     * A double between 0-1 inclusive.
     */
    private double alpha = 0.15;

    /**
     * Beta smoothing parameter used for "base" or "trend" calculation.
     * A double between 0-1 inclusive.
     */
    private double beta = 0.15;

    /**
     * Gamma smoothing parameter used for "seasonality" calculation.
     * A double between 0-1 inclusive.
     */
    private double gamma = 0.15;

    /**
     * Minimum number of data points required before the anomaly detector is ready for use.
     * A value of 0 means the detector could begin emitting anomalies immediately on first observation.
     * A minimum equivalent to "frequency" is suggested, with 2 * frequency being ideal for a lot of scenarios.
     * If no initial Base/Level/Seasonal estimate parameters are supplied, then warmUpPeriod = (2 * frequency) is an ideal minimum
     * - it allows the detector to "warm up" the seasonal components with at least 2 observations each, providing the ability
     * to calculate a standard deviation.
     */
    private int warmUpPeriod = 0;

    /**
     * Weak threshold sigmas.
     */
    private double weakSigmas = 3.0;

    /**
     * Strong threshold sigmas.
     */
    private double strongSigmas = 4.0;

    /**
     * Initial estimate for Level component.
     * Only applies if initTrainingMethod = HoltWintersTrainingMethod.NONE.
     * If not set, then 1.0 will be used for MULTIPLICATIVE seasonality and 0.0 for ADDITIVE seasonality.
     */
    private double initLevelEstimate = Double.NaN;

    /**
     * Initial estimate for Base component.
     * Only applies if initTrainingMethod = HoltWintersTrainingMethod.NONE.
     * If not set, then 1.0 will be used for MULTIPLICATIVE seasonality and 0.0 for ADDITIVE seasonality.
     */
    private double initBaseEstimate = Double.NaN;

    /**
     * Initial estimates for Seasonal components.
     * Only applies if initTrainingMethod = HoltWintersTrainingMethod.NONE.
     * Either 0 or n=frequency values must be provided.
     */
    private double[] initSeasonalEstimates = {};

    /**
     * Initial training method to use. See {@link HoltWintersTrainingMethod} for details.
     */
    private HoltWintersTrainingMethod initTrainingMethod = HoltWintersTrainingMethod.NONE;

    private final HoltWintersSeasonalEstimatesValidator seasonalEstimatesValidator = new HoltWintersSeasonalEstimatesValidator();

    /**
     * Calculates the initial training period (if applicable) based on initTrainingMethod and frequency.
     * Used to determine whether to perform training or forecasting on an observation.
     *
     * @return Length of initial training period in number of observations.
     */
    public int calculateInitTrainingPeriod() {
        return (initTrainingMethod == HoltWintersTrainingMethod.SIMPLE) ? (frequency * 2) : 0;
    }

    public HoltWintersForecaster.Params toPointForecasterParams() {
        return new HoltWintersForecaster.Params()
                .setSeasonalityType(seasonalityType)
                .setFrequency(frequency)
                .setAlpha(alpha)
                .setBeta(beta)
                .setGamma(gamma)
                .setWarmUpPeriod(warmUpPeriod)
                .setInitLevelEstimate(initLevelEstimate)
                .setInitBaseEstimate(initBaseEstimate)
                .setInitSeasonalEstimates(initSeasonalEstimates)
                .setInitTrainingMethod(initTrainingMethod);
    }

    public ExponentialWelfordIntervalForecaster.Params toIntervalForecasterParams() {

        // Currently we simply use a default alpha here. I'm not marking this as a TODO
        // since this HoltWintersParams class is deprecated anyway, and we want to start
        // using explicitly selected and tuned interval forecasters. [WLW]
        return new ExponentialWelfordIntervalForecaster.Params()
                .setAlpha(DEFAULT_EXP_WELFORD_ALPHA)
                .setInitVarianceEstimate(0.0)
                .setWeakSigmas(weakSigmas)
                .setStrongSigmas(strongSigmas);
    }

    public void validate() {
        notNull(seasonalityType, "Required: seasonalityType one of " + Arrays.toString(SeasonalityType.values()));
        notNull(initTrainingMethod, "Required: initTrainingMethod one of " + Arrays.toString(HoltWintersTrainingMethod.values()));
        isTrue(0 < frequency, "Required: frequency value greater than 0");
        isTrue(0.0 <= alpha && alpha <= 1.0, "Required: alpha in the range [0, 1]");
        isTrue(0.0 <= beta && beta <= 1.0, "Required: beta in the range [0, 1]");
        isTrue(0.0 <= gamma && gamma <= 1.0, "Required: gamma in the range [0, 1]");
        isTrue(weakSigmas > 0.0, "Required: weakSigmas > 0.0");
        isTrue(strongSigmas > weakSigmas, "Required: strongSigmas > weakSigmas");
        validateInitTrainingMethod();
        validateInitSeasonalEstimates();
    }

    private void validateInitTrainingMethod() {
        if (initTrainingMethod == HoltWintersTrainingMethod.SIMPLE) {
            int minWarmUpPeriod = calculateInitTrainingPeriod();
            if (warmUpPeriod < minWarmUpPeriod) {
                log.warn(String.format("warmUpPeriod (%d) should be greater than or equal to (frequency * 2) (%d), " +
                                "as the detector will not emit anomalies during training. Setting warmUpPeriod to %d.",
                        warmUpPeriod, minWarmUpPeriod, minWarmUpPeriod));
                warmUpPeriod = minWarmUpPeriod;
            }
        }
    }

    private void validateInitSeasonalEstimates() {
        seasonalEstimatesValidator.validate(initSeasonalEstimates, frequency, seasonalityType);
    }

}
