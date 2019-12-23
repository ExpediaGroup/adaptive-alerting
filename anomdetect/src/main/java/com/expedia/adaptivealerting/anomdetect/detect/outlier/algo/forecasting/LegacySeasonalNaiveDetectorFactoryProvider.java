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
import com.expedia.adaptivealerting.anomdetect.forecast.point.algo.seasonalnaive.SeasonalNaivePointForecaster;
import com.expedia.adaptivealerting.anomdetect.source.DetectorDocument;
import com.expedia.adaptivealerting.anomdetect.source.DetectorFactoryProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;

@Deprecated // Use ForecastingDetector with Seasonal Naive point forecaster
public class LegacySeasonalNaiveDetectorFactoryProvider implements DetectorFactoryProvider<ForecastingDetector> {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public ForecastingDetector buildDetector(DetectorDocument document) {
        val uuid = document.getUuid();

        val config = document.getConfig();
        val type = AnomalyType.valueOf((String) config.get("type"));
        val paramsMap = config.get("params");
        val legacyParams = objectMapper.convertValue(paramsMap, LegacySeasonalNaiveDetectorParams.class);
        val seasonalNaiveParams = legacyParams.toSeasonalNaiveParams();
        val welfordParams = legacyParams.toWelfordParams();

        val seasonalNaive = new SeasonalNaivePointForecaster(seasonalNaiveParams);
        val welford = new ExponentialWelfordIntervalForecaster(welfordParams);

        val trusted = document.isTrusted();

        return new ForecastingDetector(uuid, seasonalNaive, welford, type, trusted, "seasonalnaive");
    }
}
