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

import lombok.val;
import org.junit.Test;

public class IntervalForecastTest {

    @Test
    public void coverageOnly() {
        val intervalForecast = new IntervalForecast();
        intervalForecast.setUpperStrong(100.0);
        intervalForecast.setUpperWeak(90.0);
        intervalForecast.setLowerWeak(20.0);
        intervalForecast.setLowerStrong(10.0);
    }
}
