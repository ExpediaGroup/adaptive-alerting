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
package com.expedia.adaptivealerting.anomdetect.holtwinters;

import com.expedia.adaptivealerting.core.util.AssertUtil;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Arrays;

import static com.expedia.adaptivealerting.core.util.AssertUtil.isBetween;
import static com.expedia.adaptivealerting.core.util.AssertUtil.isTrue;
import static com.expedia.adaptivealerting.core.util.AssertUtil.notNull;

/**
 * @author Matt Callanan
 * @see <a href="https://otexts.org/fpp2/holt-winters.html">Holt-Winters' Seasonal Method</a>
 */
@Data
@Accessors(chain = true)
public final class HoltWintersParams {
    private static double TOLERANCE = 0.1;

    /**
     * SeasonalityType parameter used to determine which Seasonality method (Multiplicative or Additive) to use.
     */
    private SeasonalityType seasonalityType = SeasonalityType.MULTIPLICATIVE;

    /**
     * Period parameter representing periodicity of the data.
     * E.g. 24 = data is provided in hourly samples and seasons are represented as single days.
     * E.g.  7 = data is provided in daily samples and seasons are represented as single weeks.
     * E.g. 12 = data is provided in monthly samples and seasons are represented as single years.
     * E.g.  4 = data is provided in quarterly samples and seasons are represented as single years.
     */
    private int period = 0;

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
     * A minimum equivalent to "period" is suggested, with 2 * period being ideal for a lot of scenarios.
     * If no initial Base/Level/Seasonal estimate parameters are supplied, then warmUpPeriod = (2 * period) is an ideal minimum
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
     * Initial estimate for Level component.  If not set, then 1.0 will be used for MULTIPLICATIVE seasonality and 0.0 for ADDITIVE seasonality.
     */
    private double initLevelEstimate = Double.NaN;

    /**
     * Initial estimate for Base component.  If not set, then 1.0 will be used for MULTIPLICATIVE seasonality and 0.0 for ADDITIVE seasonality.
     */
    private double initBaseEstimate = Double.NaN;

    /**
     * Initial estimates for Seasonal components. n=period values must be provided.
     */
    private double[] initSeasonalEstimates = new double[]{};

    public boolean isMultiplicative() {
        return seasonalityType.equals(SeasonalityType.MULTIPLICATIVE);
    }

    public void validate() {
        notNull(seasonalityType, String.format("Required: seasonalityType one of %s", SeasonalityType.values()));
        isTrue(0 < period, "Required: period value greater than 0");
        isTrue(0.0 <= alpha && alpha <= 1.0, "Required: alpha in the range [0, 1]");
        isTrue(0.0 <= beta && beta <= 1.0, "Required: beta in the range [0, 1]");
        isTrue(0.0 <= gamma && gamma <= 1.0, "Required: gamma in the range [0, 1]");
        isTrue(weakSigmas > 0.0, "Required: weakSigmas > 0.0");
        isTrue(strongSigmas > weakSigmas, "Required: strongSigmas > weakSigmas");
        validateInitSeasonalEstimates();
    }

    private void validateInitSeasonalEstimates() {
        notNull(initSeasonalEstimates, "Required: initSeasonalEstimates must be either an empty array " +
                "or an array of n=period initial estimates for seasonal components.");
        if (initSeasonalEstimates.length > 0) {
            AssertUtil.isEqual(initSeasonalEstimates.length, period,
                    String.format("Invalid: initSeasonalEstimates size (%d) must equal period (%d)", initSeasonalEstimates.length, period));
            double seasonalSum = Arrays.stream(initSeasonalEstimates, 0, period).sum();
            if (isMultiplicative()) {
                // "With the multiplicative method, the seasonal component is expressed in relative terms (percentages).
                //  Within each year, the seasonal component will sum up to approximately m (number of periods)."
                // (from https://otexts.org/fpp2/holt-winters.html)
                // TODO HW: Determine valid tolerance, e.g. a tolerance of 0.1 means very different things for sums of 123456789.0 vs 0.1234567890
                isBetween(seasonalSum, period - TOLERANCE, period + TOLERANCE,
                        String.format("Invalid: Sum of initSeasonalEstimates (%.2f) should be approximately equal to period (%d) " +
                                "for MULTIPLICATIVE seasonality type.", seasonalSum, period));
            } else {
                // "With the additive method, the seasonal component is expressed in absolute terms in the scale of the observed series.
                //  Within each year, the seasonal component will add up to approximately zero."
                // (from https://otexts.org/fpp2/holt-winters.html)
                // TODO HW: Determine valid tolerance, e.g. a tolerance of 0.1 means very different things for sums of 123456789.0 vs 0.1234567890
                isBetween(seasonalSum, -TOLERANCE, TOLERANCE,
                        String.format("Invalid: Sum of initSeasonalEstimates (%.2f) should approximately equal 0 " +
                                "for ADDITIVE seasonality type.", seasonalSum));
            }
        }
    }
}
