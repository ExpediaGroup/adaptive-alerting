/*
 * Copyright 2018-2019 Expedia Group, Inc.
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

import com.expedia.adaptivealerting.anomdetect.comp.connector.ModelResource;
import com.expedia.adaptivealerting.anomdetect.detector.ConstantThresholdDetector;
import com.expedia.adaptivealerting.anomdetect.detector.CusumDetector;
import com.expedia.adaptivealerting.anomdetect.detector.Detector;
import com.expedia.adaptivealerting.anomdetect.detector.ForecastingDetector;
import com.expedia.adaptivealerting.anomdetect.detector.IndividualsDetector;
import com.expedia.adaptivealerting.anomdetect.detector.config.ConstantThresholdDetectorConfig;
import com.expedia.adaptivealerting.anomdetect.detector.config.CusumDetectorConfig;
import com.expedia.adaptivealerting.anomdetect.detector.config.IndividualsDetectorConfig;
import com.expedia.adaptivealerting.anomdetect.forecast.interval.ExponentialWelfordIntervalForecaster;
import com.expedia.adaptivealerting.anomdetect.forecast.point.EwmaPointForecaster;
import com.expedia.adaptivealerting.anomdetect.forecast.point.PewmaPointForecaster;
import com.expedia.adaptivealerting.anomdetect.forecast.point.holtwinters.HoltWintersPointForecaster;
import com.expedia.adaptivealerting.core.anomaly.AnomalyType;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.UUID;

import static com.expedia.adaptivealerting.core.util.AssertUtil.notNull;

/**
 * Builds detectors based on legacy detector configurations.
 */
@RequiredArgsConstructor
@Slf4j
@Deprecated
public class LegacyDetectorFactory {
    static final String CONSTANT_THRESHOLD = "constant-detector";
    static final String CUSUM = "cusum-detector";
    static final String EWMA = "ewma-detector";
    static final String HOLT_WINTERS = "holtwinters-detector";
    static final String INDIVIDUALS = "individuals-detector";
    static final String PEWMA = "pewma-detector";

    private final ObjectMapper objectMapper = new ObjectMapper();

    // TODO Currently we use a legacy process to find the detector. The legacy process couples point forecast algos
    //  with interval forecast algos. We will decouple these shortly. [WLW]
    public Detector createDetector(UUID uuid, ModelResource model) {
        notNull(uuid, "uuid can't be null");
        notNull(model, "model can't be null");

        Detector detector;
        val legacyDetectorType = model.getDetectorType().getKey();

        // Note that constant threshold, cusum and individuals are still using the original config schema.
        if (CONSTANT_THRESHOLD.equals(legacyDetectorType)) {
            detector = new ConstantThresholdDetector(uuid, toParams(model, ConstantThresholdDetectorConfig.class));
        } else if (CUSUM.equals(legacyDetectorType)) {
            detector = new CusumDetector(uuid, toParams(model, CusumDetectorConfig.class));
        } else if (EWMA.equals(legacyDetectorType)) {
            detector = createEwmaDetector(uuid, toParams(model, LegacyEwmaDetectorConfig.class));
        } else if (HOLT_WINTERS.equals(legacyDetectorType)) {
            detector = createHoltWintersDetector(uuid, toParams(model, LegacyHoltWintersDetectorConfig.class));
        } else if (INDIVIDUALS.equals(legacyDetectorType)) {
            detector = new IndividualsDetector(uuid, toParams(model, IndividualsDetectorConfig.class));
        } else if (PEWMA.equals(legacyDetectorType)) {
            detector = createPewmaDetector(uuid, toParams(model, LegacyPewmaDetectorConfig.class));
        } else {
            throw new IllegalArgumentException("Unknown detector type: " + legacyDetectorType);
        }

        log.info("Created detector: {}", detector);
        return detector;
    }

    public Detector createEwmaDetector(UUID uuid, LegacyEwmaDetectorConfig params) {
        notNull(uuid, "uuid can't be null");
        notNull(params, "params can't be null");
        params.validate();
        val pointForecaster = new EwmaPointForecaster(params.toPointForecasterParams());
        val intervalForecaster = new ExponentialWelfordIntervalForecaster(params.toIntervalForecasterParams());
        return new ForecastingDetector(uuid, pointForecaster, intervalForecaster, AnomalyType.TWO_TAILED);
    }

    public Detector createHoltWintersDetector(UUID uuid, LegacyHoltWintersDetectorConfig params) {
        notNull(uuid, "uuid can't be null");
        notNull(params, "params can't be null");
        val pointForecaster = new HoltWintersPointForecaster(params.toPointForecasterParams());
        val intervalForecaster = new ExponentialWelfordIntervalForecaster(params.toIntervalForecasterParams());
        return new ForecastingDetector(uuid, pointForecaster, intervalForecaster, AnomalyType.TWO_TAILED);
    }

    public Detector createPewmaDetector(UUID uuid, LegacyPewmaDetectorConfig params) {
        notNull(uuid, "uuid can't be null");
        notNull(params, "params can't be null");
        params.validate();
        val pointForecaster = new PewmaPointForecaster(params.toPointForecasterParams());
        val intervalForecaster = new ExponentialWelfordIntervalForecaster(params.toIntervalForecasterParams());
        return new ForecastingDetector(uuid, pointForecaster, intervalForecaster, AnomalyType.TWO_TAILED);
    }

    private <T> T toParams(ModelResource model, Class<T> paramsClass) {
        return objectMapper.convertValue(model.getParams(), paramsClass);
    }
}
