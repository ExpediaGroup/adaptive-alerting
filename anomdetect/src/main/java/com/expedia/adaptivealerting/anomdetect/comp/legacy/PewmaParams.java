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
package com.expedia.adaptivealerting.anomdetect.comp.legacy;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@Deprecated
public final class PewmaParams implements DetectorParams {

    /**
     * Smoothing param.
     */
    private double alpha = 0.15;

    /**
     * Anomaly weighting param.
     */
    private double beta = 1.0;

    /**
     * Weak anomaly threshold, in sigmas.
     */
    private double weakSigmas = 3.0;

    /**
     * Strong anomaly threshold, in sigmas.
     */
    private double strongSigmas = 4.0;

    /**
     * Initial mean estimate.
     */
    private double initMeanEstimate = 0.0;

    /**
     * How many iterations to train for.
     */
    private final int warmUpPeriod = 30;

    @Override
    public void validate() {
        // Not currently implemented
    }
}
