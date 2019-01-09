package com.expedia.adaptivealerting.anomdetect.arima;

import com.expedia.adaptivealerting.anomdetect.BasicAnomalyDetector;
import com.expedia.adaptivealerting.core.anomaly.AnomalyLevel;
import com.expedia.adaptivealerting.core.anomaly.AnomalyResult;
import com.expedia.adaptivealerting.core.anomaly.AnomalyThresholds;
import com.expedia.metrics.MetricData;
import lombok.Data;
import lombok.NonNull;

import java.util.UUID;

import static com.expedia.adaptivealerting.core.anomaly.AnomalyLevel.*;
import static com.expedia.adaptivealerting.core.util.AssertUtil.notNull;
import static java.lang.Math.abs;
import static java.lang.Math.sqrt;

/**
 * Anomaly detector implementation of
 * <a href="https://people.duke.edu/~rnau/411arim.htm">ARIMA models for time series forecasting </a>
 * the most general class of models for forecasting a time series which can be made to be “stationary” by differencing (if necessary),
 * perhaps in conjunction with nonlinear transformations such as logging or deflating (if necessary).
 *
 * @author ddivakar
 * @see <a href="https://people.duke.edu/~rnau/411arim.htm">https://people.duke.edu/~rnau/411arim.htm</a>
 */
@Data
public final class ArimaAnomalyDetector extends BasicAnomalyDetector<ArimaParams> {


    @NonNull
    private UUID uuid;

    @NonNull
    private ArimaParams params;

    /**
     * Sum of First Order Differenced Series.
     */
    private double FirstOrderDifferencedSeriesSum = 0.0;

    /**
     *  Series Sum.
     */
    private double SeriesSum = 0.0;

    /**
     * Target predicted from average mean.
     */
    private double target = 0.0;

    /**
     * Target t-1 predicted from average mean.
     */
    private double prevtarget = 0.0;

    /**
     * Previous value.
     */
    private double prevValue = 0.0;

    /**
     * Previous Difference between observed and estimated.
     */

    private double prevDiff = 0.0;

    /**
     * Total number of received data points.
     */
    private int totalDataPoints = 1;

    /**
     * Mean value of the seasonally differenced series.
     */
    private double meanofdifferences = 0.0;

    /**
     * Mean value of the series.
     */
    private double mean = 0.0;

    /**
     * μ denotes the CONSTANT  in the forecasting equation, whose estimated value is
     * mean times 1 minus the AR(1) coefficient
     */

    private double long_term_forecast_mu = 0.0;


    public ArimaAnomalyDetector() {
        this(UUID.randomUUID(), new ArimaParams());
    }

    public ArimaAnomalyDetector(ArimaParams params) {
        this(UUID.randomUUID(), params);
    }

    /**
     * Creates a new detector. Initial target is given by params.initValue and initial variance is 0.
     *
     * @param uuid   Detector UUID.
     * @param params Model params.
     */
    public ArimaAnomalyDetector(UUID uuid, ArimaParams params) {
        notNull(uuid, "uuid can't be null");
        notNull(params, "params can't be null");

        this.uuid = uuid;
        this.params = params;
        this.prevValue = params.getInitValue();
        this.target = params.getInitValue();
    }

    @Override
    protected void loadParams(ArimaParams params) {
        this.params = params;
        this.meanofdifferences = params.getInitMeanOfDifferencesEstimate();
        this.mean = params.getInitMeanEstimate();
    }

    @Override
    protected Class<ArimaParams> getParamsClass() {
        return ArimaParams.class;
    }

    @Override
    public AnomalyResult classify(MetricData metricData) {
        notNull(metricData, "metricData can't be null");

        final double observed = metricData.getValue();

        final double strongDelta = params.getStrongLimit();
        final double MA_1 = params.getMA_1();
        final double AR_1 = params.getAR_1();
        final double weakDelta = params.getWeakLimit();

        this.target = getSES(MA_1);

        AnomalyLevel level;
        if (totalDataPoints > params.getWarmUpPeriod()) {
            level = NORMAL;
            switch (params.getType()) {
                case LEFT_TAILED:
                    if (observed < (this.mean * (1 - strongDelta))) {
                        level = STRONG;
                    }
                    else if (observed < (this.mean * (1 - weakDelta))) {
                        level = WEAK;
                    }
                    break;
                case RIGHT_TAILED:
                    if (observed > (this.mean * (1 + strongDelta))) {
                        level = STRONG;
                    }
                    else if (observed > (this.mean * (1 + weakDelta))) {
                        level = WEAK;
                    }
                    break;
                case TWO_TAILED:
                    if ((observed > (this.mean * (1 + strongDelta))) || (observed < (this.mean * (1 - strongDelta)))) {
                        level = STRONG;
                    }
                    else if ((observed > (this.mean * (1 + weakDelta))) || (observed < (this.mean * (1 - weakDelta)))) {
                        level = WEAK;
                    }
                    break;
                default:
                    throw new IllegalStateException("Illegal type: " + params.getType());
            }
        } else {
            level = MODEL_WARMUP;
        }

        // update values
        this.FirstOrderDifferencedSeriesSum = this.FirstOrderDifferencedSeriesSum + this.prevDiff;
        this.SeriesSum = this.SeriesSum + observed;
        this.totalDataPoints++;
        this.meanofdifferences = getMeanFirstOrderDifferencedSeries();
        this.mean = getMean();
        long_term_forecast_mu = this.meanofdifferences * (1 - AR_1);

        this.prevDiff = observed - this.prevValue;
        this.prevValue = observed;
        this.prevtarget = this.target;


        final AnomalyResult result = new AnomalyResult(getUuid(), metricData, level);
        result.setPredicted(this.target);
        return result;
    }

    private double getMean() {
        return SeriesSum / Math.max(1, totalDataPoints - 1);
    }

    private double getMeanFirstOrderDifferencedSeries() {
        return FirstOrderDifferencedSeriesSum / Math.max(1, totalDataPoints - 1);
    }

    private double getSES(double MA_1) {
        //ARIMA(0,1,1) with constant = simple exponential smoothing with growth
        // It follows that the average age of the data in the 1-period-ahead forecasts of an ARIMA(0,1,1)-without-constant model is 1/(1-θ1).
        // et-1 = Yt-1 - Ŷt-1
        // Ŷt   =  mu + Yt-1  - θ1*et-1
        // mu = meanofdifferencesofseries * (1 - AR(1)), default AR(1) = 0
        return long_term_forecast_mu + this.prevValue - (MA_1 * (this.prevValue - this.prevtarget));
    }
}
