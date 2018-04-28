package com.expedia.adaptivealerting.core;

/**
 * Outlier level enum.
 *
 * @author Willie Wheeler
 */
public enum AnomalyLevel {
    
    /**
     * Normal data point (not an anomaly).
     */
    NORMAL,
    
    /**
     * Small anomaly.
     */
    SMALL,
    
    /**
     * Large anomaly.
     */
    LARGE
}
