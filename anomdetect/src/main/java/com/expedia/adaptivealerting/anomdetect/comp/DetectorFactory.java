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
package com.expedia.adaptivealerting.anomdetect.comp;

import com.expedia.adaptivealerting.anomdetect.detector.Detector;
import com.expedia.adaptivealerting.anomdetect.detector.DetectorConfig;
import com.expedia.adaptivealerting.anomdetect.detector.ForecastingDetector;
import com.expedia.adaptivealerting.anomdetect.forecast.interval.AdditiveIntervalForecaster;
import com.expedia.adaptivealerting.anomdetect.forecast.interval.IntervalForecaster;
import com.expedia.adaptivealerting.anomdetect.forecast.interval.IntervalForecasterParams;
import com.expedia.adaptivealerting.anomdetect.forecast.point.EwmaPointForecaster;
import com.expedia.adaptivealerting.anomdetect.forecast.point.PointForecaster;
import com.expedia.adaptivealerting.anomdetect.forecast.point.PointForecasterParams;
import lombok.val;

import java.util.UUID;

import static com.expedia.adaptivealerting.core.util.AssertUtil.notNull;

public class DetectorFactory {

    public Detector createDetector(UUID uuid, DetectorConfig config) {
        notNull(uuid, "uuid can't be null");
        notNull(config, "config can't be null");

        // TODO Think about a more elegant way to do this.
        //  It would be nice not to have to hardcode if/else logic to create detectors.
        if (config instanceof ForecastingDetector.Params) {
            return createForecastingDetector(uuid, (ForecastingDetector.Params) config);
        } else {
            throw new UnsupportedOperationException("Unsupported config type: " + config.getClass());
        }
    }

    private Detector createForecastingDetector(UUID uuid, ForecastingDetector.Params config) {
        val pointForecaster = createPointForecaster(config.getPointForecasterParams());
        val intervalForecaster = createIntervalForecaster(config.getIntervalForecasterParams());
        val anomalyType = config.getAnomalyType();
        return new ForecastingDetector(uuid, pointForecaster, intervalForecaster, anomalyType);
    }

    private PointForecaster createPointForecaster(PointForecasterParams params) {
        if (params instanceof EwmaPointForecaster.Params) {
            return new EwmaPointForecaster((EwmaPointForecaster.Params) params);
        } else {
            throw new UnsupportedOperationException("Unsupported params type: " + params.getClass());
        }
    }

    private IntervalForecaster createIntervalForecaster(IntervalForecasterParams params) {
        if (params instanceof AdditiveIntervalForecaster.Params) {
            return new AdditiveIntervalForecaster((AdditiveIntervalForecaster.Params) params);
        } else {
            throw new UnsupportedOperationException("Unsupported params type: " + params.getClass());
        }
    }
}
