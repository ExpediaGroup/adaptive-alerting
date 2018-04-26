package com.expedia.haystack.adaptivealerting;

import java.time.Instant;

/**
 * Outlier detector interface.
 *
 * @author Willie Wheeler
 */
public interface OutlierDetector {
    
    /**
     * Assigns an {@link OutlierLevel} to the given data point.
     *
     * @param instant Time instant.
     * @param value   Time series value.
     * @return Outlier level.
     */
    OutlierLevel evaluate(Instant instant, double value);
}
