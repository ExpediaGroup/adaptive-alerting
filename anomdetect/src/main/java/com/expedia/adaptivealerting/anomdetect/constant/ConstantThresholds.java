package com.expedia.adaptivealerting.anomdetect.constant;

import com.expedia.adaptivealerting.core.anomaly.AnomalyLevel;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.ToString;

import static com.expedia.adaptivealerting.core.anomaly.AnomalyLevel.NORMAL;
import static com.expedia.adaptivealerting.core.util.AssertUtil.isFalse;
import static com.expedia.adaptivealerting.core.util.AssertUtil.isTrue;

/**
 * Weak and strong thresholds to support both left, right and two-tailed tests.
 *
 * @author kashah
 */
@Data
@ToString
public class ConstantThresholds {
    private Double upperStrong;
    private Double upperWeak;
    private Double lowerStrong;
    private Double lowerWeak;

    @JsonCreator
    public ConstantThresholds(
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

    public AnomalyLevel classify(ConstantThresholdParams.Type type, double value) {
        AnomalyLevel level = NORMAL;

        switch (type) {
            case LEFT_TAILED:
                if (lowerStrong != null && value <= lowerStrong) {
                    level = AnomalyLevel.STRONG;
                } else if (lowerWeak != null && value <= lowerWeak) {
                    level = AnomalyLevel.WEAK;
                }
                break;

            case RIGHT_TAILED:
                if (upperStrong != null && value >= upperStrong) {
                    level = AnomalyLevel.STRONG;
                } else if (upperWeak != null && value >= upperWeak) {
                    level = AnomalyLevel.WEAK;
                }
                break;
            case TWO_TAILED:
                if ((upperStrong != null && value >= upperStrong) || (lowerStrong != null && value <= lowerStrong)) {
                    level = AnomalyLevel.STRONG;
                } else if ((upperWeak != null && value >= upperWeak) || (lowerWeak != null && value <= lowerWeak)) {
                    level = AnomalyLevel.WEAK;
                }
                break;
            default:
                throw new IllegalStateException("Illegal type: " + type);
        }
        return level;
    }
}

