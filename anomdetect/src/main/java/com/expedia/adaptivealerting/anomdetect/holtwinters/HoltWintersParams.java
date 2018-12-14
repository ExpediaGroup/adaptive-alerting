/*
 * Copyright 2018 Expedia Group, Inc.
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

import lombok.Data;
import lombok.experimental.Accessors;

import static com.expedia.adaptivealerting.core.util.AssertUtil.isTrue;
import static com.expedia.adaptivealerting.core.util.AssertUtil.notNull;

/**
 * @author Matt Callanan
 */
@Data
@Accessors(chain = true)
public final class HoltWintersParams {

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
     *
     */
    private int period = 0;

    /**
     * Alpha smoothing parameter used for "level" calculation.
     * A double between 0-1 inclusive.
     */
    private double alpha = 0.15;
    
    /**
     * Beta smoothing parameter used for "trend" calculation.
     * A double between 0-1 inclusive.
     */
    private double beta = 0.15;

    /**
     * Gamma smoothing parameter used for "seasonality" calculation.
     * A double between 0-1 inclusive.
     */
    private double gamma = 0.15;

    /**
     * Weak threshold sigmas.
     */
    private double weakSigmas = 3.0;
    
    /**
     * Strong threshold sigmas.
     */
    private double strongSigmas = 4.0;
    
    /**
     * Initial mean estimate.
     */
    private double initMeanEstimate = 0.0;
    
    public void validate() {
        notNull(seasonalityType, String.format("Required: seasonalityType one of %s", SeasonalityType.values()));
        isTrue(0 < period, "Required: period value greater than 0");
        isTrue(0.0 <= alpha && alpha <= 1.0, "Required: alpha in the range [0, 1]");
        isTrue(0.0 <= beta && beta <= 1.0, "Required: beta in the range [0, 1]");
        isTrue(0.0 <= gamma && gamma <= 1.0, "Required: gamma in the range [0, 1]");
        isTrue(weakSigmas > 0.0, "Required: weakSigmas > 0.0");
        isTrue(strongSigmas > weakSigmas, "Required: strongSigmas > weakSigmas");
    }
}
