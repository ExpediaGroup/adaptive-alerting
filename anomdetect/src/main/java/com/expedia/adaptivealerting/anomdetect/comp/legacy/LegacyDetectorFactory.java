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

import com.expedia.adaptivealerting.anomdetect.comp.connector.DetectorResource;
import com.expedia.adaptivealerting.anomdetect.detector.ConstantThresholdDetector;
import com.expedia.adaptivealerting.anomdetect.detector.CusumDetector;
import com.expedia.adaptivealerting.anomdetect.detector.Detector;
import com.expedia.adaptivealerting.anomdetect.detector.ForecastingDetector;
import com.expedia.adaptivealerting.anomdetect.detector.IndividualsDetector;
import com.expedia.adaptivealerting.anomdetect.forecast.interval.ExponentialWelfordIntervalForecaster;
import com.expedia.adaptivealerting.anomdetect.forecast.point.EwmaPointForecaster;
import com.expedia.adaptivealerting.anomdetect.forecast.point.HoltWintersForecaster;
import com.expedia.adaptivealerting.anomdetect.forecast.point.PewmaPointForecaster;
import com.expedia.adaptivealerting.anomdetect.detector.AnomalyType;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.UUID;

import static com.expedia.adaptivealerting.anomdetect.util.AssertUtil.notNull;

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
    public Detector createDetector(UUID uuid, DetectorResource detectorResource) {
        notNull(uuid, "uuid can't be null");
        notNull(detectorResource, "detectorResource can't be null");

        val detectorType = detectorResource.getType();
        val detectorConfig = detectorResource.getDetectorConfig();

        notNull(detectorConfig, "legacyDetectorConfig can't be null");
        val detectorParams = detectorConfig.get("params");

        Detector detector;

        // Note that constant threshold, cusum and individuals are still using the original config schema.
        if (CONSTANT_THRESHOLD.equals(detectorType)) {
            val params = toParams(detectorParams, ConstantThresholdParams.class).toNewParams();
            detector = new ConstantThresholdDetector(uuid, params);
        } else if (CUSUM.equals(detectorType)) {
            val params = toParams(detectorParams, CusumParams.class).toNewParams();
            detector = new CusumDetector(uuid, params);
        } else if (EWMA.equals(detectorType)) {
            detector = createEwmaDetector(uuid, toParams(detectorParams, EwmaParams.class));
        } else if (HOLT_WINTERS.equals(detectorType)) {
            detector = createHoltWintersDetector(uuid, toParams(detectorParams, HoltWintersParams.class));
        } else if (INDIVIDUALS.equals(detectorType)) {
            // FIXME This doesn't work with the legacy config schema. If we want this to work we'd have to do the same
            //  thing we're doing with constant threshold and cusum above. But we're not currently using individuals
            //  so we can just wait til we've migrated over to the new schema. [WLW]
            detector = new IndividualsDetector(uuid, toParams(detectorParams, IndividualsDetector.Params.class));
        } else if (PEWMA.equals(detectorType)) {
            detector = createPewmaDetector(uuid, toParams(detectorParams, PewmaParams.class));
        } else {
            throw new IllegalArgumentException("Unknown detector type: " + detectorType);
        }

        log.info("Created detector: {}", detector);
        return detector;
    }

    public Detector createEwmaDetector(UUID uuid, EwmaParams params) {
        notNull(uuid, "uuid can't be null");
        notNull(params, "params can't be null");
        params.validate();
        val pointForecaster = new EwmaPointForecaster(params.toPointForecasterParams());
        val intervalForecaster = new ExponentialWelfordIntervalForecaster(params.toIntervalForecasterParams());
        return new ForecastingDetector(uuid, pointForecaster, intervalForecaster, AnomalyType.TWO_TAILED);
    }

    public Detector createHoltWintersDetector(UUID uuid, HoltWintersParams params) {
        notNull(uuid, "uuid can't be null");
        notNull(params, "params can't be null");
        val pointForecaster = new HoltWintersForecaster(params.toPointForecasterParams());
        val intervalForecaster = new ExponentialWelfordIntervalForecaster(params.toIntervalForecasterParams());
        return new ForecastingDetector(uuid, pointForecaster, intervalForecaster, AnomalyType.TWO_TAILED);
    }

    public Detector createPewmaDetector(UUID uuid, PewmaParams params) {
        notNull(uuid, "uuid can't be null");
        notNull(params, "params can't be null");
        params.validate();
        val pointForecaster = new PewmaPointForecaster(params.toPointForecasterParams());
        val intervalForecaster = new ExponentialWelfordIntervalForecaster(params.toIntervalForecasterParams());
        return new ForecastingDetector(uuid, pointForecaster, intervalForecaster, AnomalyType.TWO_TAILED);
    }

    private <T> T toParams(Object object, Class<T> paramsClass) {
        return objectMapper.convertValue(object, paramsClass);
    }
}
