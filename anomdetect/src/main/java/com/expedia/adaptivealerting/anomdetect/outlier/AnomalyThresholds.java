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
package com.expedia.adaptivealerting.anomdetect.outlier;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

import static com.expedia.adaptivealerting.anomdetect.util.AssertUtil.isFalse;
import static com.expedia.adaptivealerting.anomdetect.util.AssertUtil.isTrue;

// TODO Rename this to IntervalForecast, but preserve "thresholds" JSON name. [WLW]

/**
 * Weak and strong thresholds to support both one- and two-tailed tests.
 */
@Data
@Setter(AccessLevel.NONE)
public class AnomalyThresholds {
    private Double upperStrong;
    private Double upperWeak;
    private Double lowerStrong;
    private Double lowerWeak;

    @JsonCreator
    public AnomalyThresholds(
            @JsonProperty("upperStrong") Double upperStrong,
            @JsonProperty("upperWeak") Double upperWeak,
            @JsonProperty("lowerWeak") Double lowerWeak,
            @JsonProperty("lowerStrong") Double lowerStrong) {

        isFalse(upperStrong == null && upperWeak == null && lowerWeak == null && lowerStrong == null,
                "At least one of the thresholds must be not null");

        if (upperStrong != null) {
            isTrue(upperWeak == null || upperStrong >= upperWeak, String.format("Required: upperStrong (%f) >= upperWeak (%f)", upperStrong, upperWeak));
            isTrue(lowerWeak == null || upperStrong >= lowerWeak, String.format("Required: upperStrong (%f) >= lowerWeak (%f)", upperStrong, lowerWeak));
            isTrue(lowerStrong == null || upperStrong >= lowerStrong, String.format("Required: upperStrong (%f) >= lowerStrong (%f)", upperStrong, lowerStrong));
        }
        if (upperWeak != null) {
            isTrue(lowerWeak == null || upperWeak >= lowerWeak, String.format("Required: upperWeak (%f) >= lowerWeak (%f)", upperWeak, lowerWeak));
            isTrue(lowerStrong == null || upperWeak >= lowerStrong, String.format("Required: upperWeak (%f) >= lowerStrong (%f)", upperWeak, lowerStrong));
        }
        if (lowerWeak != null) {
            isTrue(lowerStrong == null || lowerWeak >= lowerStrong, String.format("Required: lowerWeak (%f) >= lowerStrong (%f)", lowerWeak, lowerStrong));
        }

        this.upperStrong = upperStrong;
        this.upperWeak = upperWeak;
        this.lowerStrong = lowerStrong;
        this.lowerWeak = lowerWeak;
    }
}
