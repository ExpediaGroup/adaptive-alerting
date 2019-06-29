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
package com.expedia.adaptivealerting.anomdetect.forecast;

import com.expedia.adaptivealerting.anomdetect.forecast.IntervalForecast;
import lombok.val;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class IntervalForecastTest {
    private static final double TOLERANCE = 0.001;

    @Test
    // See https://reflectoring.io/jacoco/ for a better solution
    // The approach below creates more test code to manage
    public void coverageOnly() {
        val intervalForecast = new IntervalForecast();
        intervalForecast.setUpperStrong(100.0);
        intervalForecast.setUpperWeak(90.0);
        intervalForecast.setLowerWeak(20.0);
        intervalForecast.setLowerStrong(10.0);
    }

    @Test
    public void testConstructor() {
        val intervalForecast = new IntervalForecast(100.0, 90.0, 20.0, 10.0);
        assertEquals(100.0, intervalForecast.getUpperStrong(), TOLERANCE);
        assertEquals(90.0, intervalForecast.getUpperWeak(), TOLERANCE);
        assertEquals(20.0, intervalForecast.getLowerWeak(), TOLERANCE);
        assertEquals(10.0, intervalForecast.getLowerStrong(), TOLERANCE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_upperStrongBelowUpperWeak() {
        new IntervalForecast(90.0, 100.0, 20.0, 10.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_upperWeakBelowLowerWeak() {
        new IntervalForecast(100.0, 20.0, 90.0, 10.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstrutor_lowerWeakBelowLowerStrong() {
        new IntervalForecast(100.0, 90.0, 10.0, 20.0);
    }
}
