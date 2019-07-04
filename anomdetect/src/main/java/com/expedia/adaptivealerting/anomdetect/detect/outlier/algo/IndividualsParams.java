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
package com.expedia.adaptivealerting.anomdetect.detect.outlier.algo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.experimental.Accessors;

import static com.expedia.adaptivealerting.anomdetect.util.AssertUtil.isTrue;

@Data
@Accessors(chain = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public final class IndividualsParams {

    /**
     * Initial value estimate.
     */
    private double initValue = 0.0;

    /**
     * Minimum number of data points required before the anomaly detector is ready for use.
     */
    private int warmUpPeriod = 30;

    /**
     * Strong threshold sigmas.
     */
    private double strongSigmas = 3.0;

    /**
     * Initial mean estimate.
     */
    private double initMeanEstimate = 0.0;

    public void validate() {
        isTrue(warmUpPeriod >= 0, "Required: warmUpPeriod >= 0");
        isTrue(strongSigmas > 0.0, "Required: strongSigmas > 0.0");
    }
}
