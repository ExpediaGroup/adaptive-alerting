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
package com.expedia.adaptivealerting.anomdetect.comp.legacy;

import com.expedia.adaptivealerting.anomdetect.comp.AnomalyClassifier;
import com.expedia.adaptivealerting.anomdetect.detector.Detector;
import com.expedia.adaptivealerting.anomdetect.forecast.point.holtwinters.HoltWintersClassificationException;
import com.expedia.adaptivealerting.anomdetect.forecast.point.holtwinters.HoltWintersOnlineAlgorithm;
import com.expedia.adaptivealerting.anomdetect.forecast.point.holtwinters.HoltWintersOnlineComponents;
import com.expedia.adaptivealerting.anomdetect.forecast.point.holtwinters.HoltWintersSimpleTrainingModel;
import com.expedia.adaptivealerting.core.anomaly.AnomalyResult;
import com.expedia.adaptivealerting.core.anomaly.AnomalyThresholds;
import com.expedia.adaptivealerting.core.anomaly.AnomalyType;
import com.expedia.metrics.MetricData;
import lombok.Getter;
import lombok.val;

import java.util.UUID;

import static com.expedia.adaptivealerting.core.anomaly.AnomalyLevel.MODEL_WARMUP;
import static com.expedia.adaptivealerting.core.util.AssertUtil.notNull;
import static java.lang.String.format;

/**
 * Anomaly detector based on the Holt-Winters method, a forecasting method (a.k.a. "Triple Exponential Smoothing"). Used to capture seasonality.
 *
 * @see <a href="https://otexts.org/fpp2/holt-winters.html">Holt-Winters' Seasonal Method</a>
 */
@Deprecated
public final class HoltWintersDetector implements Detector {

    @Getter
    private UUID uuid;

    @Getter
    private HoltWintersParams params;

    @Getter
    private HoltWintersOnlineComponents components;

    private HoltWintersSimpleTrainingModel holtWintersSimpleTrainingModel;
    private HoltWintersOnlineAlgorithm holtWintersOnlineAlgorithm;
    private AnomalyClassifier classifier;

    public HoltWintersDetector(UUID uuid, HoltWintersParams params) {
        notNull(uuid, "uuid can't be null");
        notNull(params, "params can't be null");

        params.validate();

        this.uuid = uuid;
        this.params = params;

        this.components = new HoltWintersOnlineComponents(params);
        this.holtWintersOnlineAlgorithm = new HoltWintersOnlineAlgorithm();
        this.holtWintersSimpleTrainingModel = new HoltWintersSimpleTrainingModel(params);
        double initForecast = holtWintersOnlineAlgorithm.getForecast(params.getSeasonalityType(), components.getLevel(), components.getBase(), components.getSeasonal(components.getCurrentSeasonalIndex()));
        components.setForecast(initForecast);
        this.classifier = new AnomalyClassifier(AnomalyType.TWO_TAILED);
    }

    @Override
    public AnomalyResult classify(MetricData metricData) {
        notNull(metricData, "metricData can't be null");
        try {
            double prevForecast = components.getForecast();
            trainOrObserve(metricData.getValue());
            return buildAnomalyResult(metricData, prevForecast);
        } catch (Exception e) {
            throw new HoltWintersClassificationException(format("Exception occurred during classification. %s: \"%s\"", e.getClass(), e.getMessage()), e);
        }
    }

    private void trainOrObserve(double observed) {
        val params = getParams();
        if (!isInitialTrainingComplete()) {
            holtWintersSimpleTrainingModel.observeAndTrain(observed, params, components);
        } else {
            holtWintersOnlineAlgorithm.observeValueAndUpdateForecast(observed, params, components);
        }
    }

    public boolean isInitialTrainingComplete() {
        val params = getParams();
        switch (params.getInitTrainingMethod()) {
            case NONE:
                return true;
            case SIMPLE:
                return holtWintersSimpleTrainingModel.isTrainingComplete(params);
            default:
                throw new IllegalStateException(format("Unexpected training method '%s'", params.getInitTrainingMethod()));
        }
    }

    private AnomalyResult buildAnomalyResult(MetricData metricData, double prevForecast) {
        return stillWarmingUp()
                ? new AnomalyResult(MODEL_WARMUP)
                : classifyAnomaly(metricData, prevForecast);
    }

    private AnomalyResult classifyAnomaly(MetricData metricData, double prevForecast) {
        val thresholds = buildAnomalyThresholds(prevForecast);
        val level = classifier.classify(thresholds, metricData.getValue());
        return new AnomalyResult(level)
                .setPredicted(prevForecast)
                .setThresholds(thresholds);
    }

    /**
     * Identify thresholds based on previous forecast.
     */
    private AnomalyThresholds buildAnomalyThresholds(double prevForecast) {
        // TODO HW: Look at options for configuring how bands are defined
        val params = getParams();
        double stddev = components.getSeasonalStandardDeviation(components.getCurrentSeasonalIndex());
        final double weakDelta = params.getWeakSigmas() * stddev;
        final double strongDelta = params.getStrongSigmas() * stddev;

        return new AnomalyThresholds(
                prevForecast + strongDelta,
                prevForecast + weakDelta,
                prevForecast - weakDelta,
                prevForecast - strongDelta);
    }

    private boolean stillWarmingUp() {
        val params = getParams();
        return components.getN() <= params.getWarmUpPeriod();
    }
}
