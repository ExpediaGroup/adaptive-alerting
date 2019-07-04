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
package com.expedia.adaptivealerting.anomdetect.forecast.interval;

import com.expedia.metrics.MetricData;

public interface IntervalForecaster {

    // FIXME An interval forecaster shouldn't require a point forecast. It can use one, but it shouldn't be required.
    //  Instead, if the interval forecaster needs a point forecast, it should take a point forecaster in the constructor
    //  and then use that to generate the point forecast. [WLW]
    IntervalForecast forecast(MetricData metricData, double pointForecast);
}
