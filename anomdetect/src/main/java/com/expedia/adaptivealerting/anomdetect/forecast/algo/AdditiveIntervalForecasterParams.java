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
package com.expedia.adaptivealerting.anomdetect.forecast.algo;

import com.expedia.adaptivealerting.anomdetect.forecast.IntervalForecasterParams;
import lombok.Data;
import lombok.experimental.Accessors;

import static com.expedia.adaptivealerting.anomdetect.util.AssertUtil.isTrue;

@Data
@Accessors(chain = true)
public final class AdditiveIntervalForecasterParams implements IntervalForecasterParams {
    private double weakValue;
    private double strongValue;

    @Override
    public void validate() {
        isTrue(weakValue >= 0.0, "Required: weakValue >= 0.0");
        isTrue(strongValue >= weakValue, "Required: strongValue >= weakValue");
    }
}
