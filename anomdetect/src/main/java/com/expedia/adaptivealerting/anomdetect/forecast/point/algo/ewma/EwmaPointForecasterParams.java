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
package com.expedia.adaptivealerting.anomdetect.forecast.point.algo.ewma;

import com.expedia.adaptivealerting.anomdetect.util.AlgoParams;
import lombok.Data;
import lombok.experimental.Accessors;

import static com.expedia.adaptivealerting.anomdetect.util.AssertUtil.isBetween;

@Data
@Accessors(chain = true)
public final class EwmaPointForecasterParams implements AlgoParams {

    /**
     * Smoothing param. Somewhat misnamed because higher values lead to less smoothing, but it's called the smoothing
     * parameter in the literature.
     */
    private double alpha = 0.15;

    /**
     * Initial mean estimate.
     */
    public double initMeanEstimate = 0.0;

    @Override
    public void validate() {
        isBetween(alpha, 0.0, 1.0, "Required: 0.0 <= alpha <= 1.0");
    }
}
