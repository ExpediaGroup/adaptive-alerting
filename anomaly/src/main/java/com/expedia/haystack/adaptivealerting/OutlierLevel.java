package com.expedia.haystack.adaptivealerting;

/**
 * Outlier level enum.
 *
 * @author Willie Wheeler
 */
public enum OutlierLevel {
    
    /**
     * Normal data point (not an outlier).
     */
    NORMAL,
    
    /**
     * Small outlier.
     */
    SMALL,
    
    /**
     * Large outlier.
     */
    LARGE
}
