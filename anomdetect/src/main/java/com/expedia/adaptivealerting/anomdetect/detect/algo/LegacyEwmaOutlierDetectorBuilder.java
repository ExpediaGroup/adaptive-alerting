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
package com.expedia.adaptivealerting.anomdetect.detect.algo;

import com.expedia.adaptivealerting.anomdetect.detect.AnomalyType;
import com.expedia.adaptivealerting.anomdetect.detect.DetectorDocument;
import com.expedia.adaptivealerting.anomdetect.detect.ForecastingOutlierDetector;
import com.expedia.adaptivealerting.anomdetect.forecast.algo.EwmaPointForecaster;
import com.expedia.adaptivealerting.anomdetect.forecast.algo.EwmaPointForecasterParams;
import com.expedia.adaptivealerting.anomdetect.forecast.PointForecaster;
import lombok.val;

import java.util.Map;

import static com.expedia.adaptivealerting.anomdetect.util.AssertUtil.notNull;

/**
 * @deprecated EWMA is a forecasting algorithm, not an anomaly detection algorithm. We need to be able to mix and match
 * point and interval forecasters.
 */
@Deprecated
public class LegacyEwmaOutlierDetectorBuilder extends LegacyForecastingOutlierDetectorBuilder {

    @Override
    public ForecastingOutlierDetector build(DetectorDocument document) {
        notNull(document, "document can't be null");
        val uuid = document.getUuid();
        val config = document.getDetectorConfig();
        val paramsMap = (Map<String, Object>) config.get("params");
        val pointForecaster = toPointForecaster(paramsMap);
        val intervalForecaster = toIntervalForecaster(paramsMap);
        val anomalyType = toAnomalyType(paramsMap);
        return new ForecastingOutlierDetector(uuid, pointForecaster, intervalForecaster, anomalyType);
    }

    private PointForecaster toPointForecaster(Map<String, Object> paramsMap) {
        val alpha = (Double) paramsMap.get("alpha");
        val initMeanEstimate = (Double) paramsMap.get("initMeanEstimate");

        val params = new EwmaPointForecasterParams();
        if (alpha != null) {
            params.setAlpha(alpha);
        }
        if (initMeanEstimate != null) {
            params.setInitMeanEstimate(initMeanEstimate);
        }

        return new EwmaPointForecaster(params);
    }

    // TODO We'll want this for all of the forecasting detectors. [WLW]
    private AnomalyType toAnomalyType(Map<String, Object> paramsMap) {
        val type = (String) paramsMap.get("type");
        return type == null ? AnomalyType.RIGHT_TAILED : AnomalyType.valueOf(type);
    }
}
