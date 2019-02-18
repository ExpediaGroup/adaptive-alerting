package com.expedia.adaptivealerting.anomdetect.arima;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author Dinil Divakar
 */
@Data
@Accessors(chain = true)
public final class ArimaDetectorParams {

    public enum Type {
        LEFT_TAILED,
        RIGHT_TAILED,
        TWO_TAILED
    }

    /**
     * Detector type: left-, right- or two-tailed.
     */
    private Type type = Type.RIGHT_TAILED;

    /**
     * Initial mean estimate.
     */
    private double initValue = 0.0;

    /**
     * Minimum number of data points required before the anomaly detector is ready for use.
     */
    private int warmUpPeriod = 14;

    /**
     * Upper Outlier level multiplier - this value times the 95% confidence
     * interval determines the upper limit.
     *Will impact outlier detection depending on Type being RIGHT LEFT or TWO TAILED.
     */
    private double strongLimitMultiplier = 1.5;

    /**
     * Lower Outlier level multiplier - this value times the 95% confidence
     * interval determines the lower limit.
     * Will impact outlier detection depending on Type being RIGHT LEFT or TWO TAILED.
     */
     private double weakLimitMultiplier = 0.67;

    /**
     * number of autoregressive terms,  non seasonal
     */
    private int p = 1;

    /**
     * number of nonseasonal differences needed for stationarity
     */
    private int d = 1;

    /**
     * number of lagged forecast errors in the prediction equation, non seasonal
     */
    private int q = 1;

    /**
     * seasonal AR order
     */
    private int seasonalp = 0;

    /**
     * seasonal differencing
     */
    private int seasonald = 0;

    /**
     * seasonal MA order
     */
    private int seasonalq = 0;

    /**
     * time span of repeating seasonal pattern
     */
    private int m = 0;

    /**
     *  number of future data points to forecast
     */
    private int forecastsize = 1;

    /**
     *  number of historical data points used to predict
     */
    private int historylength = 100;

}