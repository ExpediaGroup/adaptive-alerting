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
import com.expedia.adaptivealerting.anomdetect.detector.IndividualsDetector;
import com.expedia.adaptivealerting.anomdetect.detector.ForecastingDetector;
import com.expedia.adaptivealerting.anomdetect.forecast.interval.ExponentialWelfordIntervalForecaster;
import com.expedia.adaptivealerting.anomdetect.forecast.point.EwmaPointForecaster;
import com.expedia.adaptivealerting.anomdetect.forecast.point.PewmaPointForecaster;
import com.expedia.adaptivealerting.core.anomaly.AnomalyType;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import lombok.var;

import java.util.Map;
import java.util.UUID;

import static com.expedia.adaptivealerting.core.util.AssertUtil.notNull;

/**
 * Builds detectors based on legacy detector configurations.
 */
@RequiredArgsConstructor
@Slf4j
@Deprecated
public class LegacyDetectorFactory {
    private final ObjectMapper objectMapper = new ObjectMapper();

    // TODO Currently we use a legacy process to find the detector. The legacy process couples point forecast algos
    //  with interval forecast algos. We will decouple these shortly. [WLW]
    public Detector createDetector(UUID uuid, ModelResource modelResource) {
        notNull(uuid, "uuid can't be null");
        notNull(modelResource, "modelResource can't be null");

        val detectorType = modelResource.getDetectorType().getKey();
        val paramsMap = modelResource.getParams();
        var detector = (Detector) null;

        if (LegacyDetectorTypes.CONSTANT_THRESHOLD.equals(detectorType)) {
            detector = createConstantThresholdDetector(uuid, toParams(paramsMap, ConstantThresholdDetector.Params.class));
        } else if (LegacyDetectorTypes.CUSUM.equals(detectorType)) {
            detector = createCusumDetector(uuid, toParams(paramsMap, CusumDetector.Params.class));
        } else if (LegacyDetectorTypes.EWMA.equals(detectorType)) {
            detector = createEwmaDetector(uuid, toParams(paramsMap, EwmaParams.class));
        } else if (LegacyDetectorTypes.HOLT_WINTERS.equals(detectorType)) {
            detector = createHoltWintersDetector(uuid, toParams(paramsMap, HoltWintersParams.class));
        } else if (LegacyDetectorTypes.INDIVIDUALS.equals(detectorType)) {
            detector = createIndividualsDetector(uuid, toParams(paramsMap, IndividualsDetector.Params.class));
        } else if (LegacyDetectorTypes.PEWMA.equals(detectorType)) {
            detector = createPewmaDetector(uuid, toParams(paramsMap, PewmaParams.class));
        } else {
            throw new IllegalArgumentException("Unknown detector type: " + detectorType);
        }

        log.info("Created detector: {}", detector);
        return detector;
    }

    public Detector createConstantThresholdDetector(UUID uuid, ConstantThresholdDetector.Params params) {
        notNull(uuid, "uuid can't be null");
        notNull(params, "params can't be null");
        params.validate();
        return new ConstantThresholdDetector(uuid, params);
    }

    public Detector createCusumDetector(UUID uuid, CusumDetector.Params params) {
        notNull(uuid, "uuid can't be null");
        notNull(params, "params can't be null");
        params.validate();
        return new CusumDetector(uuid, params);
    }

    public Detector createEwmaDetector(UUID uuid, EwmaParams params) {
        notNull(uuid, "uuid can't be null");
        notNull(params, "params can't be null");
        params.validate();
        val pointForecaster = new EwmaPointForecaster(params.toPointForecasterParams());
        val intervalForecaster = new ExponentialWelfordIntervalForecaster(params.toIntervalForecasterParams());
        val anomalyType = AnomalyType.TWO_TAILED;
        return new ForecastingDetector(uuid, pointForecaster, intervalForecaster, anomalyType);
    }

    public Detector createHoltWintersDetector(UUID uuid, HoltWintersParams params) {
        notNull(uuid, "uuid can't be null");
        notNull(params, "params can't be null");
        params.validate();
        return new HoltWintersDetector(uuid, params);
    }

    public Detector createIndividualsDetector(UUID uuid, IndividualsDetector.Params params) {
        notNull(uuid, "uuid can't be null");
        notNull(params, "params can't be null");
        params.validate();
        return new IndividualsDetector(uuid, params);
    }

    public Detector createPewmaDetector(UUID uuid, PewmaParams params) {
        notNull(uuid, "uuid can't be null");
        notNull(params, "params can't be null");
        params.validate();
        val pointForecaster = new PewmaPointForecaster(params.toPointForecasterParams());
        val intervalForecaster = new ExponentialWelfordIntervalForecaster(params.toIntervalForecasterParams());
        val anomalyType = AnomalyType.TWO_TAILED;
        return new ForecastingDetector(uuid, pointForecaster, intervalForecaster, anomalyType);
    }

    private <T> T toParams(Map<String, Object> paramsMap, Class<T> paramsClass) {
        return objectMapper.convertValue(paramsMap, paramsClass);
    }
}
