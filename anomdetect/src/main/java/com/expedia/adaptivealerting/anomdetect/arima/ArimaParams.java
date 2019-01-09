package com.expedia.adaptivealerting.anomdetect.arima;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author ddivakar
 */
@Data
@Accessors(chain = true)
public final class ArimaParams {

    public enum Type {
        LEFT_TAILED,
        RIGHT_TAILED,
        TWO_TAILED
    }

    /**
     * Detector type: left-, right- or two-tailed.
     */
    private Type type = Type.TWO_TAILED;

    /**
     * Initial mean estimate.
     */
    private double initValue = 0.0;

    /**
     * Minimum number of data points required before the anomaly detector is ready for use.
     */
    private int warmUpPeriod = 15;

    /**
     * Upper Outlier level.
     */
    private double strongLimit = 0.95;

    /**
     * Initial mean of differences estimate.
     */
    private double initMeanOfDifferencesEstimate = 0.0;

    /**
     * Initial mean estimate.
     */
    private double initMeanEstimate = 0.0;

    /**
     * Initial mean estimate.
     */
    private double MA_1 = 0.1548;

    /**
     * AR(1) coefficient used to calculate the constant.
     */
    private double AR_1 = 0;

    /**
     * Lower confidence level, multiplied by mean of the series to get the limit.
     */
    private double weakLimit = 0.5;
}
