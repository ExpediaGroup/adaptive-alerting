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
package com.expedia.adaptivealerting.anomdetect.forecast.point.algo.sma;

import java.util.List;

import com.expedia.adaptivealerting.anomdetect.util.AlgoParams;

import lombok.Data;
import lombok.experimental.Accessors;

import static com.expedia.adaptivealerting.anomdetect.util.AssertUtil.isStrictlyPositive;
import static com.expedia.adaptivealerting.anomdetect.util.AssertUtil.isTrue;

@Data
@Accessors(chain = true)
public final class SmaPointForecasterParams implements AlgoParams {

    /**
     * How many previous observations to include in the average.
     */
    private int lookBackPeriod = Integer.MAX_VALUE;

    /**
     * An optional period's worth of values to seed the forecaster with.
     * If specified, length of list must equal lookBackPeriod.
     */
    private List<Double> initialPeriodOfValues;

    @Override
    public void validate() {
        isStrictlyPositive(lookBackPeriod, "Required: lookBackPeriod > 0");
        isTrue(initialPeriodOfValuesValid(), "When specified, initialPeriodOfValues.size must equal lookBackPeriod");
    }

    private boolean initialPeriodOfValuesValid() {
        return initialPeriodOfValues == null ||
            initialPeriodOfValues.size() == lookBackPeriod;
    }
}
