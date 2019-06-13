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
import com.expedia.adaptivealerting.anomdetect.outlier.forecast.point.EwmaPointForecaster;
import lombok.Data;
import lombok.experimental.Accessors;

import static com.expedia.adaptivealerting.anomdetect.util.AssertUtil.isTrue;

@Data
@Accessors(chain = true)
@Deprecated
public final class EwmaParams {

    /**
     * Smoothing param. Somewhat misnamed because higher values lead to less smoothing, but it's called the
     * smoothing parameter in the literature.
     */
    private double alpha = 0.15;

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

    public EwmaPointForecaster.Params toPointForecasterParams() {
        return new EwmaPointForecaster.Params()
                .setAlpha(alpha)
                .setInitMeanEstimate(initMeanEstimate);
    }

    public ExponentialWelfordIntervalForecaster.Params toIntervalForecasterParams() {
        return new ExponentialWelfordIntervalForecaster.Params()
                .setAlpha(alpha)
                .setInitVarianceEstimate(0.0)
                .setWeakSigmas(weakSigmas)
                .setStrongSigmas(strongSigmas);
    }

    public void validate() {
        isTrue(0.0 <= alpha && alpha <= 1.0, "Required: alpha in the range [0, 1]");
        isTrue(weakSigmas > 0.0, "Required: weakSigmas > 0.0");
        isTrue(strongSigmas > weakSigmas, "Required: strongSigmas > weakSigmas");
    }
}
