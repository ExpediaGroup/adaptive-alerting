package com.expedia.adaptivealerting.anomdetect.arima;

/* This package uses https://github.com/Workday/timeseries-forecast Java open source library.
 * Adding the copyright notice and permission notice as required by the libary's license.

 * Copyright 2017 Workday, Inc.

 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit
 * persons to whom the Software is furnished to do so, subject to the following conditions:

 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 * Software.

 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
*/
import com.workday.insights.timeseries.arima.Arima;
import com.workday.insights.timeseries.arima.struct.ForecastResult;
import com.workday.insights.timeseries.arima.struct.ArimaParams;
import com.workday.insights.timeseries.arima.struct.ArimaModel;
import com.workday.insights.timeseries.arima.ArimaSolver;
import com.expedia.adaptivealerting.anomdetect.BasicAnomalyDetector;
import com.expedia.adaptivealerting.core.anomaly.AnomalyLevel;
import com.expedia.adaptivealerting.core.anomaly.AnomalyResult;
import com.expedia.metrics.MetricData;
import lombok.Data;
import lombok.NonNull;

import java.util.UUID;

import static com.expedia.adaptivealerting.core.anomaly.AnomalyLevel.*;
import static com.expedia.adaptivealerting.core.util.AssertUtil.notNull;
import java.util.ArrayList;


/**
 ** <a href="https://people.duke.edu/~rnau/411arim.htm">ARIMA models for time series forecasting</a>
 **
 ** ARIMA models for time series forecasting the most general
 ** class of models for forecasting a time series which can be made to be “stationary” by differencing(if necessary),
 ** perhaps in conjunction with nonlinear transformations such as logging or deflating(if necessary).
 **
 ** Anomaly detector implementation of ARIMA(0,1,1) for default input parameters.
 **
 */
@Data
public final class ArimaAnomalyDetector extends BasicAnomalyDetector<ArimaDetectorParams> {


    @NonNull
    private UUID uuid;

    @NonNull
    private ArimaDetectorParams params;

    /**
     * Target predicted.
     */
    private double target = 0.0;

    /**
     *  Check for number of history points, update dataset accordingly.
     */
    private int historylengthchecker = 0;

    /*Anomalies have to be validated agianst previous limits for the observation
    * as the current observation is not yet loaded/known.
    */
    double previousupper = 0.0;
    double previouslower = 0.0;

    /**
     *  Input data for training and prediction.
     */
    private ArrayList<Double> dataArray = new ArrayList<Double> ();


    public ArimaAnomalyDetector() {
        this(UUID.randomUUID(), new ArimaDetectorParams());
    }

    public ArimaAnomalyDetector(ArimaDetectorParams params) {
        this(UUID.randomUUID(), params);
    }

    /**
     * Creates a new detector. Initial target is given by params.initValue and initial variance is 0.
     *
     * @param uuid   Detector UUID.
     * @param params Model params.
     */

    public ArimaAnomalyDetector(UUID uuid, ArimaDetectorParams params) {
        notNull(uuid, "uuid can't be null");
        notNull(params, "params can't be null");

        this.uuid = uuid;
        this.params = params;
        this.target = params.getInitValue();
    }

    @Override
    protected void loadParams(ArimaDetectorParams params) {
        this.params = params;
    }


    @Override
    protected Class<ArimaDetectorParams> getParamsClass() {
        return ArimaDetectorParams.class;
    }

    @Override
    public AnomalyResult classify(MetricData metricData) {
        notNull(metricData, "metricData can't be null");

        final double observed = metricData.getValue();
        final int historylength = params.getHistorylength();
        System.out.println("Less than or not");
        System.out.println(historylengthchecker);
        System.out.println(historylength);
        System.out.println("Less than or not");

        if (historylengthchecker < historylength) {
            System.out.printf("adding data: %f", observed);
            dataArray.add(observed);
            historylengthchecker++;
        }
        else {
            dataArray.remove(0);
            dataArray.add(observed);
        }

        final double strongDelta = params.getStrongLimitMultiplier();
        final double weakDelta = params.getWeakLimitMultiplier();

        final int p = params.getP();
        final int d = params.getD();
        final int q = params.getQ();
        final int P = params.getSeasonalp();
        final int D = params.getSeasonald();
        final int Q = params.getSeasonalq();
        final int m = params.getM();
        final int forecastSize = params.getForecastsize();
        /*seasonal parameters P,D,Q,m. If D or m is less than 1,
        * then the model is understood to be non-seasonal and the seasonal parameters P,D,Q,m will have no effect.
        */

        ArimaParams arimamodelparams = new ArimaParams(p,d,q,P,D,Q,m);
        AnomalyLevel level;

        if (historylengthchecker > params.getWarmUpPeriod()) {
            level = NORMAL;
            double[] dataArrayfromList = new double[dataArray.size()];
            for (int i = 0; i < dataArrayfromList.length; i++) {
                dataArrayfromList[i] = dataArray.get(i);
            }

            System.out.println("Original data");
            for (int j = 0; j < dataArray.size(); j++) {
                System.out.println(dataArray.get(j));
            }
            System.out.println("End of original data");

            for (int k = 0; k < dataArrayfromList.length; k++) {
                System.out.println(dataArrayfromList[k]);
            }
            System.out.println("End of data");


            final ArimaModel fittedModel = ArimaSolver.estimateARIMA(arimamodelparams, dataArrayfromList, dataArrayfromList.length,
                    dataArrayfromList.length + 1);
            ForecastResult forecastResult = Arima.forecast_arima(dataArrayfromList, forecastSize, arimamodelparams);

            // Read forecast values
            double[] forecastData = forecastResult.getForecast();
            this.target = forecastData[0];

            if (params.getType() == ArimaDetectorParams.Type.TWO_TAILED) {
                if (observed > previousupper || observed < previouslower) {
                    level = WEAK;
                }
                if (observed > (previousupper* strongDelta) || observed < (previouslower * weakDelta)) {
                    level = STRONG;
                }
            }
            else if (params.getType() == ArimaDetectorParams.Type.RIGHT_TAILED) {
                System.out.printf("lower: %f\n",(previouslower* strongDelta));
                System.out.println(observed);
                System.out.printf("upper: %f\n",(previousupper* strongDelta));
                if (observed > (previouslower * strongDelta)) {
                    level = WEAK;
                }
                if (observed > (previousupper * strongDelta) ) {
                    level = STRONG;
                }
            }
            else {
                if (observed < (previousupper * weakDelta)) {
                    level = WEAK;
                }
                if (observed < (previousupper * weakDelta) ) {
                    level = STRONG;
                }
            }
            // Obtain upper- and lower-bounds of confidence intervals on forecast values.
            // By default, it computes at 95%-confidence level. This value can be adjusted in ForecastUtil.java in library.
            double[] uppers = forecastResult.getForecastUpperConf();
            double[] lowers = forecastResult.getForecastLowerConf();
            previousupper = uppers[0];
            previouslower = lowers[0];

            System.out.printf("%s\n", params.getType());
        }
        else {
            level = MODEL_WARMUP;
        }

        if (historylengthchecker == (params.getWarmUpPeriod()+1)) {
            level = MODEL_WARMUP; // first observation after training cannot be marked as anomaly against limits.
        }

        final AnomalyResult result = new AnomalyResult(getUuid(), metricData, level);
        result.setPredicted(this.target);
        return result;

    }

}
