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
package com.expedia.adaptivealerting.anomdetect.cusum;

import com.expedia.adaptivealerting.core.anomaly.AnomalyType;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author Karan Shah
 * @author Willie Wheeler
 */
@Data
@Accessors(chain = true)
public final class CusumParams {

    /**
     * Detector type: left-, right- or two-tailed.
     */
    private AnomalyType type;

    /**
     * Target value (i.e., the set point).
     */
    private double targetValue = 0.0;

    /**
     * Weak threshold sigmas.
     */
    private double weakSigmas = 3.0;

    /**
     * Strong threshold sigmas.
     */
    private double strongSigmas = 4.0;

    /**
     * Slack param to calculate slack value k where k = slack_param * stdev.
     */
    private double slackParam = 0.5;

    /**
     * Initial mean estimate.
     */
    private double initMeanEstimate = 0.0;

    /**
     * Minimum number of data points required before this anomaly detector is available for use.
     */
    private int warmUpPeriod = 25;
}
