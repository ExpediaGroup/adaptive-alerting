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
package com.expedia.adaptivealerting.anomdetect.forecast.point.algo.seasonalnaive;

import com.expedia.adaptivealerting.anomdetect.util.AlgoParams;
import lombok.Data;
import lombok.experimental.Accessors;

import static com.expedia.adaptivealerting.anomdetect.util.AssertUtil.isStrictlyPositive;

/**
 * Configuration parameters for the {@link SeasonalNaivePointForecaster}.
 */
@Data
@Accessors(chain = true)
public class SeasonalNaivePointForecasterParams implements AlgoParams {

    /**
     * Number of observations per cycle.
     */
    private int cycleLength;

    /**
     * Number of seconds between two observations.
     */
    private int intervalLength;

    @Override
    public void validate() {
        isStrictlyPositive(cycleLength, "Required: cycleLength > 0");
        isStrictlyPositive(intervalLength, "Required: intervalLength > 0");
    }
}
