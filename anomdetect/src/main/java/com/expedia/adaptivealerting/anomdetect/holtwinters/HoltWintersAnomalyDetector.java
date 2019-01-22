/*
 * Copyright 2019 Expedia Group, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.expedia.adaptivealerting.anomdetect.holtwinters;

import com.expedia.adaptivealerting.anomdetect.BasicAnomalyDetector;
import com.expedia.adaptivealerting.core.anomaly.AnomalyLevel;
import com.expedia.adaptivealerting.core.anomaly.AnomalyResult;
import com.expedia.adaptivealerting.core.anomaly.AnomalyThresholds;
import com.expedia.metrics.MetricData;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;

import java.util.UUID;

import static com.expedia.adaptivealerting.core.util.AssertUtil.notNull;

/**
 * Anomaly detector based on the Holt-Winters method, a forecasting method (a.k.a. "Triple Exponential Smoothing"). Used to capture seasonality.
 *
 * @author Matt Callanan
 * @see <a href="https://otexts.org/fpp2/holt-winters.html">Holt-Winters' Seasonal Method</a>
 */
@EqualsAndHashCode(callSuper = true)
@Data
public final class HoltWintersAnomalyDetector extends BasicAnomalyDetector<HoltWintersParams> {

    @NonNull
    private HoltWintersParams params;
    private HoltWintersOnlineComponents components;
    private HoltWintersOnlineAlgorithm holtWintersOnlineAlgorithm;
    private boolean headerNotPrinted = true;

    public HoltWintersAnomalyDetector() {
        this(UUID.randomUUID(), new HoltWintersParams());
    }

    public HoltWintersAnomalyDetector(HoltWintersParams params) {
        this(UUID.randomUUID(), params);
    }

    public HoltWintersAnomalyDetector(UUID uuid, HoltWintersParams params) {
        notNull(uuid, "uuid can't be null");
        notNull(params, "params can't be null");

        params.validate();

        setUuid(uuid);
        loadParams(params);
        components = new HoltWintersOnlineComponents(params);
        holtWintersOnlineAlgorithm = new HoltWintersOnlineAlgorithm();
    }

    @Override
    protected void loadParams(HoltWintersParams params) {
        this.params = params;
    }

    @Override
    protected Class<HoltWintersParams> getParamsClass() {
        return HoltWintersParams.class;
    }

    @Override
    public AnomalyResult classify(MetricData metricData) {
        return classify(metricData, false);
    }

    // TODO HW: Remove 'debug' param
    public AnomalyResult classify(MetricData metricData, boolean debug) {
        notNull(metricData, "metricData can't be null");

        final double observed = metricData.getValue();
        // components holds the forecast previously predicted for this observation
        double lastForecast = components.getForecast();

        // Identify thresholds based on previously observed values
        // TODO HW: Look at options for configuring how bands are defined
        // TODO HW: Add model warmup param and anomaly level. See e.g. CUSUM, Individuals, PEWMA. [WLW]
        double stddev = components.getSeasonalStandardDeviation(components.currentSeasonalIndex());
        final double weakDelta = params.getWeakSigmas() * stddev;
        final double strongDelta = params.getStrongSigmas() * stddev;

        final AnomalyThresholds thresholds = new AnomalyThresholds(
                lastForecast + strongDelta,
                lastForecast + weakDelta,
                lastForecast - strongDelta,
                lastForecast - weakDelta);

        holtWintersOnlineAlgorithm.observeValueAndUpdateForecast(observed, params, components);
        double newForecast = components.getForecast();

        // TODO HW: The first n=period observations will result in STRONG level anomalies due to stddev = 0.0 - should we ignore them and report NORMAL?  This is the same for Ewma
        // TODO HW: Should we provide the ability to use the first n=period observations as the initial estimates for the seasonal components?  E.g. Provide a boolean 'learnInitSeasonalEstimates' parameter.

        final AnomalyLevel anomalyLevel = thresholds.classifyExclusiveBounds(observed);
        AnomalyResult anomalyResult = new AnomalyResult(getUuid(), metricData, anomalyLevel);
        if (debug) printStats(observed, weakDelta, strongDelta, lastForecast, newForecast, stddev, anomalyLevel);
        return anomalyResult;
    }

    private void printStats(double observed, double weakDelta, double strongDelta, double oldForecast, double newForecast, double stddev, AnomalyLevel anomalyLevel) {
        if (headerNotPrinted) {
            System.out.println(
                    "         n, " +
                    "         y, " +
                    "         l, " +
                    "         b, " +
                    "         s, " +
                    "         i, " +
                    "  prevPred, " +
                    "      pred, " +
                    " anomLevel, " +
                    "  upperStr, " +
                    " upperWeak, " +
                    "  lowerStr, " +
                    " lowerWeak, " +
                    "       min, " +
                    "       max, " +
                    "      mean, " +
                    "         var, " +
                    "     stdev");
            headerNotPrinted = false;
        }
        System.out.printf("%10d, " +
                        "%10.0f, " +
                        "%10.2f, %10.2f, %10.2f, %10d, " +
                        "%10.2f, %10.2f, %10s, " +
                        "%10.2f, %10.2f, %10.2f, %10.2f, " +
                        "%10.0f, %10.0f, %10.2f, %12.2f, %10.2f\n",
                components.getN(),
                observed,
                components.getLevel(), components.getBase(), components.getSeasonal(components.previousSeasonalIndex()), components.previousSeasonalIndex(),
                oldForecast, newForecast, anomalyLevel,
                components.getMean() + strongDelta, components.getMean() + weakDelta, components.getMean() - strongDelta, components.getMean() - weakDelta,
                components.getMin(), components.getMax(), components.getMean(), components.getVariance(), stddev);
    }

}
