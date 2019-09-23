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

import com.expedia.adaptivealerting.anomdetect.AlgoParams;
import lombok.Data;
import lombok.experimental.Accessors;

import static com.expedia.adaptivealerting.anomdetect.util.AssertUtil.isBetween;
import static com.expedia.adaptivealerting.anomdetect.util.AssertUtil.isTrue;

@Data
@Accessors(chain = true)
public final class ExponentialWelfordIntervalForecasterParams implements AlgoParams {
    private double alpha = 0.15;
    private double initVarianceEstimate = 0.0;
    private double weakSigmas = 3.0;
    private double strongSigmas = 4.0;
    // TODO Add warmup period

    @Override
    public void validate() {
        isBetween(alpha, 0.0, 1.0, "Required: 0.0 <= alpha <= 1.0");
        isTrue(initVarianceEstimate >= 0.0, "Required: initVarianceEstimate >= 0.0");
        isTrue(weakSigmas >= 0.0, "Required: weakSigmas >= 0.0");
        isTrue(strongSigmas >= weakSigmas, "Required: strongSigmas >= weakSigmas");
    }
}
