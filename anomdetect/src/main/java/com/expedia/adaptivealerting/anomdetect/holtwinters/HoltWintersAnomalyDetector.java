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

import static com.expedia.adaptivealerting.core.anomaly.AnomalyLevel.MODEL_WARMUP;
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
    @NonNull
    private HoltWintersOnlineComponents components;
    @NonNull
    private HoltWintersOnlineAlgorithm holtWintersOnlineAlgorithm;

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
        double initForecast = holtWintersOnlineAlgorithm.getForecast(params.getSeasonalityType(), components.getLevel(), components.getBase(), components.getSeasonal(components.getCurrentSeasonalIndex()));
        components.setForecast(initForecast);
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
        notNull(metricData, "metricData can't be null");

        final double observed = metricData.getValue();
        // components holds the forecast previously predicted for this observation
        double prevForecast = components.getForecast();

        // Identify thresholds based on previously observed values
        // TODO HW: Look at options for configuring how bands are defined
        double stddev = components.getSeasonalStandardDeviation(components.getCurrentSeasonalIndex());
        final double weakDelta = params.getWeakSigmas() * stddev;
        final double strongDelta = params.getStrongSigmas() * stddev;

        final AnomalyThresholds thresholds = new AnomalyThresholds(
                prevForecast + strongDelta,
                prevForecast + weakDelta,
                prevForecast - strongDelta,
                prevForecast - weakDelta);

        holtWintersOnlineAlgorithm.observeValueAndUpdateForecast(observed, params, components);

        final AnomalyLevel anomalyLevel;
        if (stillWarmingUp()) {
            anomalyLevel = MODEL_WARMUP;
        } else {
            anomalyLevel = thresholds.classify(observed);
        }

        // TODO HW: Add a 'initialLearningMethod' parameter. To begin with there will be two accepted values:
        //   "NONE" that uses either the user-provided init*Estimate params or the default values if none provided for initial l, b, and s components
        //   "SIMPLE" that implements Hyndman's "simple" method for selecting initial state values.
        //            (https://github.com/robjhyndman/forecast/blob/master/R/HoltWintersNew.R#L61-L67)
        //            I.e. it uses the first 2 seasons to calculate what the l, b, and s components were for the season immediately preceding the
        //            first observation. Need to ensure that warmUpPeriod is >= (period * 2) to ensure no anomalies are emitted during learning period.
        // Other learning methods may be added to this enum at a later time.

        AnomalyResult result = new AnomalyResult(getUuid(), metricData, anomalyLevel);
        result.setPredicted(prevForecast);
        result.setThresholds(thresholds);
        return result;
    }

    private boolean stillWarmingUp() {
        return components.getN() <= params.getWarmUpPeriod();
    }
}
