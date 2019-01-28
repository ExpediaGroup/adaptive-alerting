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
package com.expedia.adaptivealerting.core.anomaly;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.ToString;

import static com.expedia.adaptivealerting.core.util.AssertUtil.isFalse;
import static com.expedia.adaptivealerting.core.util.AssertUtil.isTrue;

/**
 * Weak and strong thresholds to support both one- and two-tailed tests.
 *
 * @author Willie Wheeler
 */
@Data
@ToString
public class AnomalyThresholds {
    private Double upperStrong;
    private Double upperWeak;
    private Double lowerStrong;
    private Double lowerWeak;

    @JsonCreator
    public AnomalyThresholds(
            @JsonProperty("upperStrong") Double upperStrong,
            @JsonProperty("upperWeak") Double upperWeak,
            @JsonProperty("lowerStrong") Double lowerStrong,
            @JsonProperty("lowerWeak") Double lowerWeak) {

        isFalse(upperStrong == null && upperWeak == null && lowerWeak == null && lowerStrong == null,
                "At least one of the thresholds must be not null");

        if (upperStrong != null) {
            isTrue(upperWeak == null || upperStrong >= upperWeak, "Required: upperStrong >= upperWeak");
            isTrue(lowerWeak == null || upperStrong >= lowerWeak, "Required: upperStrong >= lowerWeak");
            isTrue(lowerStrong == null || upperStrong >= lowerStrong, "Required: upperStrong >= lowerStrong");
        }
        if (upperWeak != null) {
            isTrue(lowerWeak == null || upperWeak >= lowerWeak, "Required: upperWeak >= lowerWeak");
            isTrue(lowerStrong == null || upperWeak >= lowerStrong, "Required: upperWeak >= lowerStrong");
        }
        if (lowerWeak != null) {
            isTrue(lowerStrong == null || lowerWeak >= lowerStrong, "Required: lowerWeak >= lowerStrong");
        }

        this.upperStrong = upperStrong;
        this.upperWeak = upperWeak;
        this.lowerStrong = lowerStrong;
        this.lowerWeak = lowerWeak;
    }

    public AnomalyLevel classify(double value) {
        if (upperStrong != null && value >= upperStrong) {
            return AnomalyLevel.STRONG;
        } else if (upperWeak != null && value >= upperWeak) {
            return AnomalyLevel.WEAK;
        } else if (lowerStrong != null && value <= lowerStrong) {
            return AnomalyLevel.STRONG;
        } else if (lowerWeak != null && value <= lowerWeak) {
            return AnomalyLevel.WEAK;
        } else {
            return AnomalyLevel.NORMAL;
        }
    }

    //Method to classify values for detectors which use tails. [KS]
    public AnomalyLevel classify(AnomalyType type, double value) {
        switch (type) {
            case LEFT_TAILED:
                if (lowerStrong != null && value <= lowerStrong) {
                    return AnomalyLevel.STRONG;
                } else if (lowerWeak != null && value <= lowerWeak) {
                    return AnomalyLevel.WEAK;
                } else {
                    return AnomalyLevel.NORMAL;
                }
            case RIGHT_TAILED:
                if (upperStrong != null && value >= upperStrong) {
                    return AnomalyLevel.STRONG;
                } else if (upperWeak != null && value >= upperWeak) {
                    return AnomalyLevel.WEAK;
                } else {
                    return AnomalyLevel.NORMAL;
                }
            case TWO_TAILED:
                if ((upperStrong != null && value >= upperStrong) || (lowerStrong != null && value <= lowerStrong)) {
                    return AnomalyLevel.STRONG;
                } else if ((upperWeak != null && value >= upperWeak) || (lowerWeak != null && value <= lowerWeak)) {
                    return AnomalyLevel.WEAK;
                } else {
                    return AnomalyLevel.NORMAL;
                }
            default:
                throw new IllegalStateException("Illegal type: " + type);
        }
    }

    /**
     * Legacy classification to handle exclusive bounds, since some of the detectors were using this previously, and
     * hence have unit tests that expect it.
     *
     * @param value Value to classify.
     * @return Anomaly classification.
     */
    @Deprecated
    public AnomalyLevel classifyExclusiveBounds(double value) {
        if (upperStrong != null && value > upperStrong) {
            return AnomalyLevel.STRONG;
        } else if (upperWeak != null && value > upperWeak) {
            return AnomalyLevel.WEAK;
        } else if (lowerStrong != null && value < lowerStrong) {
            return AnomalyLevel.STRONG;
        } else if (lowerWeak != null && value < lowerWeak) {
            return AnomalyLevel.WEAK;
        } else {
            return AnomalyLevel.NORMAL;
        }
    }
}
