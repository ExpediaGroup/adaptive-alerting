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
    private HoltWintersSimpleTrainingModel holtWintersSimpleTrainingModel;
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
        holtWintersSimpleTrainingModel = new HoltWintersSimpleTrainingModel(params);
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
        double prevForecast = components.getForecast();
        trainOrObserve(metricData.getValue());
        return buildAnomalyResult(metricData, prevForecast);
    }

    private void trainOrObserve(double observed) {
        if (!isInitialTrainingComplete()) {
            holtWintersSimpleTrainingModel.observeAndTrain(observed, params, components);
        } else {
            holtWintersOnlineAlgorithm.observeValueAndUpdateForecast(observed, params, components);
        }
    }

    public boolean isInitialTrainingComplete() {
        switch (params.getInitTrainingMethod()) {
            case NONE: return true;
            case SIMPLE: return holtWintersSimpleTrainingModel.isTrainingComplete(params);
            default: throw new IllegalStateException(String.format("Unexpected training method '%s'", params.getInitTrainingMethod()));
        }
    }

    private AnomalyResult buildAnomalyResult(MetricData metricData, double prevForecast) {
        return stillWarmingUp()
                ? new AnomalyResult(getUuid(), metricData, MODEL_WARMUP)
                : classifyAnomaly(metricData, prevForecast);
    }

    private AnomalyResult classifyAnomaly(MetricData metricData, double prevForecast) {
        AnomalyThresholds thresholds = buildAnomalyThresholds(prevForecast);
        AnomalyLevel level = thresholds.classify(metricData.getValue());
        AnomalyResult result = new AnomalyResult(getUuid(), metricData, level);
        result.setPredicted(prevForecast);
        result.setThresholds(thresholds);
        return result;
    }

    /**
     * Identify thresholds based on previous forecast.
     */
    private AnomalyThresholds buildAnomalyThresholds(double prevForecast) {
        // TODO HW: Look at options for configuring how bands are defined
        double stddev = components.getSeasonalStandardDeviation(components.getCurrentSeasonalIndex());
        final double weakDelta = params.getWeakSigmas() * stddev;
        final double strongDelta = params.getStrongSigmas() * stddev;

        return new AnomalyThresholds(
                prevForecast + strongDelta,
                prevForecast + weakDelta,
                prevForecast - strongDelta,
                prevForecast - weakDelta);
    }

    private boolean stillWarmingUp() {
        return components.getN() <= params.getWarmUpPeriod();
    }
}
