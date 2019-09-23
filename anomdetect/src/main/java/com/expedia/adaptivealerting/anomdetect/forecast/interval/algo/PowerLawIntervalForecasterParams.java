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

import static com.expedia.adaptivealerting.anomdetect.util.AssertUtil.isTrue;

@Data
@Accessors(chain = true)
public final class PowerLawIntervalForecasterParams implements AlgoParams {
    private double alpha;
    private double beta;
    private double weakMultiplier;
    private double strongMultiplier;

    @Override
    public void validate() {
        isTrue(alpha > 0.0, "Required: alpha >= 0.0");
        isTrue(beta > 0.0, "Required: beta >= 0.0");
        isTrue(weakMultiplier >= 0.0, "Required: weakMultiplier >= 0.0");
        isTrue(strongMultiplier >= weakMultiplier, "Required: strongMultiplier >= weakMultiplier");
    }
}
