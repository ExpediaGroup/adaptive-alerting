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

import com.expedia.adaptivealerting.anomdetect.AlgoParams;
import com.expedia.adaptivealerting.anomdetect.detect.AnomalyType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.experimental.Accessors;

import static com.expedia.adaptivealerting.anomdetect.util.AssertUtil.isTrue;
import static com.expedia.adaptivealerting.anomdetect.util.AssertUtil.notNull;

@Data
@Accessors(chain = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CusumParams implements AlgoParams {

    /**
     * Target value (i.e., the set point).
     */
    private double targetValue = 0.0;

    /**
     * Slack param to calculate slack value k where k = slack_param * stdev.
     */
    private double slackParam = 0.5;

    /**
     * Initial mean estimate.
     */
    // FIXME This should be a hyperparam
    private double initMeanEstimate = 0.0;

    /**
     * Weak threshold sigmas.
     */
    // FIXME This should be a hyperparam
    private double weakSigmas = 3.0;

    /**
     * Strong threshold sigmas.
     */
    // FIXME This should be a hyperparam
    private double strongSigmas = 4.0;

    /**
     * Detector type: left-, right- or two-tailed.
     */
    // FIXME This should be a detector config
    private AnomalyType type;

    /**
     * Minimum number of data points required before this anomaly detector is available for use.
     */
    private int warmUpPeriod = 25;

    @Override
    public void validate() {
        notNull(type, "type can't be null");
        isTrue(weakSigmas > 0.0, "Required: weakSigmas > 0.0");
        isTrue(strongSigmas > weakSigmas, "Required: strongSigmas > weakSigmas");
        // TODO slackParam?
        isTrue(warmUpPeriod >= 0, "Required: warmUpPeriod >= 0");
    }
}
