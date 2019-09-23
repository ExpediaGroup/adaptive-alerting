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
import com.expedia.metrics.MetricData;
import com.expedia.metrics.MetricDefinition;
import lombok.val;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class SeasonalNaivePointForecasterTest {
    private static final double TOLERANCE = 0.001;

    private MetricDefinition metricDef = new MetricDefinition("some-key");

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_notNull() {
        new SeasonalNaivePointForecaster(null);
    }

    @Test
    public void testForecast() {
        val params = new SeasonalNaivePointForecasterParams().setCycleLength(3);
        val forecasterUnderTest = new SeasonalNaivePointForecaster(params);

        MetricData metricData;
        PointForecast forecast;

        // First we have to initialize the forecaster.
        // Here we're just filling up the buffer forecast.

        metricData = new MetricData(metricDef, 10.0, 0L);
        forecast = forecasterUnderTest.forecast(metricData);
        assertNull(forecast);

        metricData = new MetricData(metricDef, 20.0, 1L);
        forecast = forecasterUnderTest.forecast(metricData);
        assertNull(forecast);

        metricData = new MetricData(metricDef, 30.0, 2L);
        forecast = forecasterUnderTest.forecast(metricData);
        assertNull(forecast);

        // Now the buffer is full, so we can forecast.

        metricData = new MetricData(metricDef, 40.0, 3L);
        forecast = forecasterUnderTest.forecast(metricData);
        assertEquals(10.0, forecast.getValue(), TOLERANCE);

        metricData = new MetricData(metricDef, 50.0, 4L);
        forecast = forecasterUnderTest.forecast(metricData);
        assertEquals(20.0, forecast.getValue(), TOLERANCE);

        metricData = new MetricData(metricDef, 60.0, 5L);
        forecast = forecasterUnderTest.forecast(metricData);
        assertEquals(30.0, forecast.getValue(), TOLERANCE);

        // Second cycle through

        metricData = new MetricData(metricDef, 70.0, 6L);
        forecast = forecasterUnderTest.forecast(metricData);
        assertEquals(40.0, forecast.getValue(), TOLERANCE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testForecast_nullMetricData() {
        new SeasonalNaivePointForecaster(new SeasonalNaivePointForecasterParams()).forecast(null);
    }
}
