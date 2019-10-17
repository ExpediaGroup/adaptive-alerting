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
package com.expedia.adaptivealerting.anomdetect.detect.outlier.algo.forecasting;

import com.expedia.adaptivealerting.anomdetect.detect.AnomalyType;
import com.expedia.adaptivealerting.anomdetect.forecast.interval.algo.expwelford.ExponentialWelfordIntervalForecaster;
import com.expedia.adaptivealerting.anomdetect.forecast.point.algo.pewma.PewmaPointForecaster;
import com.expedia.adaptivealerting.anomdetect.source.DetectorDocument;
import com.expedia.adaptivealerting.anomdetect.source.DetectorFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;

@RequiredArgsConstructor
@Deprecated // Use ForecastingDetector with EWMA point forecaster
public class LegacyPewmaDetectorFactory implements DetectorFactory<ForecastingDetector> {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @NonNull
    private DetectorDocument document;

    @Override
    public ForecastingDetector buildDetector() {
        val uuid = document.getUuid();

        val config = document.getDetectorConfig();
        val type = AnomalyType.valueOf((String) config.get("type"));
        val paramsMap = config.get("params");
        val legacyParams = objectMapper.convertValue(paramsMap, LegacyPewmaDetectorParams.class);
        val pewmaParams = legacyParams.toPewmaParams();
        val welfordParams = legacyParams.toWelfordParams();

        val pewma = new PewmaPointForecaster(pewmaParams);
        val welford = new ExponentialWelfordIntervalForecaster(welfordParams);

        return new ForecastingDetector(uuid, pewma, welford, type);
    }
}
