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
package com.expedia.adaptivealerting.anomdetect.forecast.point.algo.seasonalnaive;

import com.expedia.adaptivealerting.anomdetect.forecast.point.PointForecast;
import com.expedia.adaptivealerting.anomdetect.forecast.point.algo.seasonalnaive.SeasonalNaivePointForecaster;
import com.expedia.adaptivealerting.anomdetect.forecast.point.algo.seasonalnaive.SeasonalNaivePointForecasterParams;
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
    public void testForecast_allDatapointsPresent() {
        val params = new SeasonalNaivePointForecasterParams().setCycleLength(5).setIntervalLength(10);
        val forecasterUnderTest = new SeasonalNaivePointForecaster(params);

        MetricData metricData;
        PointForecast forecast;

        // First we have to initialize the forecaster.
        // Here we're just filling up the buffer forecast.

        metricData = new MetricData(metricDef, 10.0, 1563428100L);
        forecast = forecasterUnderTest.forecast(metricData);
        assertNull(forecast);

        metricData = new MetricData(metricDef, 20.0, 1563428110L);
        forecast = forecasterUnderTest.forecast(metricData);
        assertNull(forecast);

        metricData = new MetricData(metricDef, 30.0, 1563428120L);
        forecast = forecasterUnderTest.forecast(metricData);
        assertNull(forecast);

        metricData = new MetricData(metricDef, 40.0, 1563428130L);
        forecast = forecasterUnderTest.forecast(metricData);
        assertNull(forecast);

        metricData = new MetricData(metricDef, 50.0, 1563428140L);
        forecast = forecasterUnderTest.forecast(metricData);
        assertNull(forecast);

        // Now the buffer is full, we can forecast.

        metricData = new MetricData(metricDef, 60.0, 1563428150L);
        forecast = forecasterUnderTest.forecast(metricData);
        assertEquals(10.0, forecast.getValue(), TOLERANCE);

        metricData = new MetricData(metricDef, 70.0, 1563428160L);
        forecast = forecasterUnderTest.forecast(metricData);
        assertEquals(20.0, forecast.getValue(), TOLERANCE);
    }

    @Test
    public void testForecast_missingOneDatapoint() {
        val params = new SeasonalNaivePointForecasterParams().setCycleLength(5).setIntervalLength(10);
        val forecasterUnderTest = new SeasonalNaivePointForecaster(params);

        MetricData metricData;
        PointForecast forecast;

        // First we have to initialize the forecaster.
        // Here we're just filling up the buffer forecast.

        metricData = new MetricData(metricDef, 10.0, 1563428100L);
        forecast = forecasterUnderTest.forecast(metricData);
        assertNull(forecast);
        // we miss one observation here
        metricData = new MetricData(metricDef, 20.0, 1563428120L);
        forecast = forecasterUnderTest.forecast(metricData);
        assertNull(forecast);

        metricData = new MetricData(metricDef, 30.0, 1563428130L);
        forecast = forecasterUnderTest.forecast(metricData);
        assertNull(forecast);

        metricData = new MetricData(metricDef, 40.0, 1563428140L);
        forecast = forecasterUnderTest.forecast(metricData);
        assertNull(forecast);

        // Now the buffer is full, so we can forecast.

        metricData = new MetricData(metricDef, 60.0, 1563428150L);
        forecast = forecasterUnderTest.forecast(metricData);
        assertEquals(10.0, forecast.getValue(), TOLERANCE);

        // next observation is a null value
        metricData = new MetricData(metricDef, 90.0, 1563428260L);
        forecast = forecasterUnderTest.forecast(metricData);
        assertNull(forecast);
    }

    @Test
    public void testForecast_missingTwoDatapoints() {
        val params = new SeasonalNaivePointForecasterParams().setCycleLength(5).setIntervalLength(10);
        val forecasterUnderTest = new SeasonalNaivePointForecaster(params);

        MetricData metricData;
        PointForecast forecast;

        // First we have to initialize the forecaster.
        // Here we're just filling up the buffer forecast.

        metricData = new MetricData(metricDef, 10.0, 1563428100L);
        forecast = forecasterUnderTest.forecast(metricData);
        assertNull(forecast);

        metricData = new MetricData(metricDef, 20.0, 1563428130L);
        forecast = forecasterUnderTest.forecast(metricData);
        assertNull(forecast);

        metricData = new MetricData(metricDef, 30.0, 1563428140L);
        forecast = forecasterUnderTest.forecast(metricData);
        assertNull(forecast);


        metricData = new MetricData(metricDef, 40.0, 1563428150L);
        forecast = forecasterUnderTest.forecast(metricData);
        assertEquals(10.0, forecast.getValue(), TOLERANCE);

        metricData = new MetricData(metricDef, 50.0, 1563428160L);
        forecast = forecasterUnderTest.forecast(metricData);
        assertNull(forecast);
    }


    @Test
    public void testForecast_missingTwoDatapointsWithWrap() {
        val params = new SeasonalNaivePointForecasterParams().setCycleLength(5).setIntervalLength(10);
        val forecasterUnderTest = new SeasonalNaivePointForecaster(params);

        MetricData metricData;
        PointForecast forecast;

        // First we have to initialize the forecaster.
        // Here we're just filling up the buffer forecast.

        metricData = new MetricData(metricDef, 10.0, 1563428100L);
        forecast = forecasterUnderTest.forecast(metricData);
        assertNull(forecast);
        // we miss one observation here
        metricData = new MetricData(metricDef, 20.0, 1563428110L);
        forecast = forecasterUnderTest.forecast(metricData);
        assertNull(forecast);

        metricData = new MetricData(metricDef, 30.0, 1563428120L);
        forecast = forecasterUnderTest.forecast(metricData);
        assertNull(forecast);

        metricData = new MetricData(metricDef, 40.0, 1563428130L);
        forecast = forecasterUnderTest.forecast(metricData);
        assertNull(forecast);

        // we will miss the last and then the first position in the buffer now

        // and do the forecast

        metricData = new MetricData(metricDef, 50.0, 1563428160L);
        forecast = forecasterUnderTest.forecast(metricData);
        assertEquals(20.0, forecast.getValue(), TOLERANCE);

        metricData = new MetricData(metricDef, 60.0, 1563428170L);
        forecast = forecasterUnderTest.forecast(metricData);
        assertEquals(30.0, forecast.getValue(), TOLERANCE);

        metricData = new MetricData(metricDef, 70.0, 1563428180L);
        forecast = forecasterUnderTest.forecast(metricData);
        assertEquals(40.0, forecast.getValue(), TOLERANCE);

        // next observation is a null value
        metricData = new MetricData(metricDef, 80.0, 1563428290L);
        forecast = forecasterUnderTest.forecast(metricData);
        assertNull(forecast);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testForecast_nullMetricData() {
        new SeasonalNaivePointForecaster(new SeasonalNaivePointForecasterParams()).forecast(null);
    }
}
