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
package com.expedia.adaptivealerting.anomdetect.forecast.point.algo;

import com.expedia.adaptivealerting.anomdetect.AlgoParams;
import lombok.Data;
import lombok.experimental.Accessors;

import static com.expedia.adaptivealerting.anomdetect.util.AssertUtil.isBetween;
import static com.expedia.adaptivealerting.anomdetect.util.AssertUtil.isTrue;

@Data
@Accessors(chain = true)
public class PewmaPointForecasterParams implements AlgoParams {

    // TODO Describe why we chose these defaults as appropriate.
    //  For example if the paper recommends them, we should say that.

    // TODO Include anomaly type (left/right/two-tailed)

    /**
     * Smoothing param.
     */
    private double alpha = 0.15;

    /**
     * Anomaly weighting param.
     */
    private double beta = 1.0;

    /**
     * Initial mean estimate.
     */
    private double initMeanEstimate = 0.0;

    /**
     * How many iterations to train for.
     */
    private int warmUpPeriod = 30;

    @Override
    public void validate() {
        isBetween(alpha, 0.0, 1.0, "Required: 0.0 <= alpha <= 1.0");
        isBetween(beta, 0.0, 1.0, "Required: 0.0 <= beta <= 1.0");
        isTrue(warmUpPeriod >= 0, "Required: warmUpPeriod >= 0");
    }
}
