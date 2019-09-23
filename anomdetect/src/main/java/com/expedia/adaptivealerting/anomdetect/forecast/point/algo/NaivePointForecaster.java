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
package com.expedia.adaptivealerting.anomdetect.forecast.point.algo;

import com.expedia.adaptivealerting.anomdetect.forecast.point.PointForecast;
import com.expedia.adaptivealerting.anomdetect.forecast.point.PointForecaster;
import com.expedia.metrics.MetricData;
import lombok.val;

import static com.expedia.adaptivealerting.anomdetect.util.AssertUtil.notNull;

// TODO Support drift method. See https://otexts.com/fpp2/simple-methods.html#simple-methods.

/**
 * Implements the naive forecaster described in https://otexts.com/fpp2/simple-methods.html. Naive forecasts are
 * optimal for random walk series, but they can be effective for other series as well. Accordingly, this algorithm is
 * also known as random walk forecasting.
 */
public class NaivePointForecaster implements PointForecaster {
    private MetricData lastMetricData;

    @Override
    public PointForecast forecast(MetricData metricData) {
        notNull(metricData, "metricData can't be null");
        val forecastMetricData = lastMetricData;
        this.lastMetricData = metricData;
        return forecastMetricData == null ? null : new PointForecast(forecastMetricData.getValue(), false);
    }
}
