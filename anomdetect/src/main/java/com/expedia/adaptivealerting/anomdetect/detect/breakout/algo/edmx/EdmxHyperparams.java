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
package com.expedia.adaptivealerting.anomdetect.detect.breakout.algo.edmx;

import com.expedia.adaptivealerting.anomdetect.util.AlgoParams;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.experimental.Accessors;

import static com.expedia.adaptivealerting.anomdetect.util.AssertUtil.isBetween;
import static com.expedia.adaptivealerting.anomdetect.util.AssertUtil.isTrue;

@Data
@Accessors(chain = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class EdmxHyperparams implements AlgoParams {

    /**
     * Minimum window size.
     */
    private int delta = 6;

    /**
     * Size of the buffer we pass to the EDM-X breakout estimator.
     */
    private int bufferSize = 15;

    /**
     * Number of permutations to use when estimating the p-value.
     */
    private int numPerms = 99;

    /**
     * Significance level for the strong breakout significance test.
     */
    private double strongAlpha = 0.01;

    /**
     * Significance level for the weak breakout significance test.
     */
    private double weakAlpha = 0.05;

    @Override
    public void validate() {
        isTrue(delta > 0, "Required: delta > 0");
        isTrue(bufferSize >= 2 * delta, "Required: bufferSize >= 2 * delta");
        isTrue(numPerms >= 0, "Required: numPerms >= 0");
        isBetween(strongAlpha, 0.0, 1.0, "Required: 0.0 <= alpha <= 1.0");
        isBetween(weakAlpha, 0.0, 1.0, "Required: 0.0 <= weakAlpha <= 1.0");
        isTrue(weakAlpha > strongAlpha, "Required: weakAlpha > strongAlpha");
    }
}
