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
package com.expedia.adaptivealerting.anomdetect.detector.config;

import com.expedia.adaptivealerting.anomdetect.detector.ForecastingDetector;
import com.expedia.adaptivealerting.anomdetect.forecast.interval.IntervalForecasterParams;
import com.expedia.adaptivealerting.anomdetect.forecast.point.PointForecasterParams;
import com.expedia.adaptivealerting.core.anomaly.AnomalyType;
import lombok.Data;
import lombok.experimental.Accessors;

import static com.expedia.adaptivealerting.core.util.AssertUtil.notNull;

/**
 * {@link ForecastingDetector} configuration object.
 */
@Data
@Accessors(chain = true)
public final class ForecastingDetectorConfig extends AbstractDetectorConfig {
    private PointForecasterParams pointForecasterParams;
    private IntervalForecasterParams intervalForecasterParams;
    private AnomalyType anomalyType;

    @Override
    public void validate() {
        notNull(pointForecasterParams, "pointForecasterParams can't be null");
        notNull(intervalForecasterParams, "intervalForecasterParams can't be null");
        notNull(anomalyType, "anomalyType can't be null");
    }
}
