package com.expedia.adaptivealerting.core.anomaly;

import lombok.Data;

@Data
public class AnomalyType {
    public enum Type {
        LEFT_TAILED,
        RIGHT_TAILED,
        TWO_TAILED
    }

    /**
     * Detector type: left-, right- or two-tailed.
     */
    private Type type = Type.LEFT_TAILED;
}
