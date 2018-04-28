package com.expedia.adaptivealerting.core;

import java.time.Instant;

/**
 * Outlier detector interface.
 *
 * @author Willie Wheeler
 */
public interface AnomalyDetector {
    
    /**
     * Assigns an {@link AnomalyLevel} to the given data point.
     *
     * @param instant Time instant.
     * @param value   Time series value.
     * @return Outlier level.
     */
    AnomalyLevel evaluate(Instant instant, double value);
}
