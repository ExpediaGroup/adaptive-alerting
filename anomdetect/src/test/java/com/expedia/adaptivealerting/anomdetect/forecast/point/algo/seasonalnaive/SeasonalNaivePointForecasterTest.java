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
import com.expedia.metrics.MetricData;
import com.expedia.metrics.MetricDefinition;
import lombok.val;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

// TODO: Also test what happens when timestamps do not align perfectly with interval
// TODO: Make these tests more data-driven for readability
public class SeasonalNaivePointForecasterTest {
    private static final double TOLERANCE = 0.001;
    private static final int CYCLE_LENGTH = 5;
    private static final int INTERVAL_LENGTH = 10;
    private static final long FIRST_CYCLE_FIRST_SLOT = 1563428100L;
    private static final long FIRST_CYCLE_SECOND_SLOT = FIRST_CYCLE_FIRST_SLOT + INTERVAL_LENGTH;
    private static final long FIRST_CYCLE_THIRD_SLOT = FIRST_CYCLE_FIRST_SLOT + (INTERVAL_LENGTH * 2);
    private static final long FIRST_CYCLE_FOURTH_SLOT = FIRST_CYCLE_FIRST_SLOT + (INTERVAL_LENGTH * 3);
    private static final long FIRST_CYCLE_FIFTH_SLOT = FIRST_CYCLE_FIRST_SLOT + (INTERVAL_LENGTH * 4);
    private static final long SECOND_CYCLE_FIRST_SLOT = FIRST_CYCLE_FIRST_SLOT + (INTERVAL_LENGTH * 5);
    private static final long SECOND_CYCLE_SECOND_SLOT = FIRST_CYCLE_FIRST_SLOT + (INTERVAL_LENGTH * 6);
    private static final long SECOND_CYCLE_THIRD_SLOT = FIRST_CYCLE_FIRST_SLOT + (INTERVAL_LENGTH * 7);
    private static final long SECOND_CYCLE_FOURTH_SLOT = FIRST_CYCLE_FIRST_SLOT + (INTERVAL_LENGTH * 8);
    private static final long SECOND_CYCLE_FIFTH_SLOT = FIRST_CYCLE_FIRST_SLOT + (INTERVAL_LENGTH * 9);
    private static final double FIRST_CYCLE_FIRST_SLOT_VALUE = 10.0;
    private static final double FIRST_CYCLE_SECOND_SLOT_VALUE = 20.0;
    private static final double FIRST_CYCLE_THIRD_SLOT_VALUE = 30.0;
    private static final double FIRST_CYCLE_FOURTH_SLOT_VALUE = 40.0;
    private static final double FIRST_CYCLE_FIFTH_SLOT_VALUE = 50.0;
    private static final double SECOND_CYCLE_FIRST_SLOT_VALUE = 60.0;
    private static final double SECOND_CYCLE_SECOND_SLOT_VALUE = 70.0;
    private static final double SECOND_CYCLE_THIRD_SLOT_VALUE = 80.0;
    private static final double SECOND_CYCLE_FOURTH_SLOT_VALUE = 90.0;
    private static final double SECOND_CYCLE_FIFTH_SLOT_VALUE = 100.0;

    private MetricDefinition metricDef = new MetricDefinition("some-key");

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_notNull() {
        new SeasonalNaivePointForecaster(null);
    }

    @Test
    public void testForecast_allDatapointsPresent() {
        val params = new SeasonalNaivePointForecasterParams().setCycleLength(CYCLE_LENGTH).setIntervalLength(INTERVAL_LENGTH);
        val forecasterUnderTest = new SeasonalNaivePointForecaster(params);

        MetricData metricData;
        PointForecast forecast;

        // First we have to initialize the forecaster.
        // Here we're just filling up the buffer forecast.

        metricData = new MetricData(metricDef, FIRST_CYCLE_FIRST_SLOT_VALUE, FIRST_CYCLE_FIRST_SLOT);
        forecast = forecasterUnderTest.forecast(metricData);
        assertNull(forecast);

        metricData = new MetricData(metricDef, FIRST_CYCLE_SECOND_SLOT_VALUE, FIRST_CYCLE_SECOND_SLOT);
        forecast = forecasterUnderTest.forecast(metricData);
        assertNull(forecast);

        metricData = new MetricData(metricDef, FIRST_CYCLE_THIRD_SLOT_VALUE, FIRST_CYCLE_THIRD_SLOT);
        forecast = forecasterUnderTest.forecast(metricData);
        assertNull(forecast);

        metricData = new MetricData(metricDef, FIRST_CYCLE_FOURTH_SLOT_VALUE, FIRST_CYCLE_FOURTH_SLOT);
        forecast = forecasterUnderTest.forecast(metricData);
        assertNull(forecast);

        metricData = new MetricData(metricDef, FIRST_CYCLE_FIFTH_SLOT_VALUE, FIRST_CYCLE_FIFTH_SLOT);
        forecast = forecasterUnderTest.forecast(metricData);
        assertNull(forecast);

        // Now the buffer is full, we can forecast.

        metricData = new MetricData(metricDef, SECOND_CYCLE_FIRST_SLOT_VALUE, SECOND_CYCLE_FIRST_SLOT);
        forecast = forecasterUnderTest.forecast(metricData);
        assertEquals(FIRST_CYCLE_FIRST_SLOT_VALUE, forecast.getValue(), TOLERANCE);

        metricData = new MetricData(metricDef, SECOND_CYCLE_SECOND_SLOT_VALUE, SECOND_CYCLE_SECOND_SLOT);
        forecast = forecasterUnderTest.forecast(metricData);
        assertEquals(FIRST_CYCLE_SECOND_SLOT_VALUE, forecast.getValue(), TOLERANCE);
    }

    @Test
    public void testForecast_missingOneDatapoint() {
        val params = new SeasonalNaivePointForecasterParams().setCycleLength(5).setIntervalLength(10);
        val forecasterUnderTest = new SeasonalNaivePointForecaster(params);

        MetricData metricData;
        PointForecast forecast;

        // Fill buffer with first cycle but with second slot missing

        metricData = new MetricData(metricDef, FIRST_CYCLE_FIRST_SLOT_VALUE, FIRST_CYCLE_FIRST_SLOT);
        forecasterUnderTest.forecast(metricData);

        // [Skip one datapoint here]

        metricData = new MetricData(metricDef, FIRST_CYCLE_SECOND_SLOT_VALUE, FIRST_CYCLE_THIRD_SLOT);
        forecasterUnderTest.forecast(metricData);

        metricData = new MetricData(metricDef, FIRST_CYCLE_THIRD_SLOT_VALUE, FIRST_CYCLE_FOURTH_SLOT);
        forecasterUnderTest.forecast(metricData);

        metricData = new MetricData(metricDef, FIRST_CYCLE_FOURTH_SLOT_VALUE, FIRST_CYCLE_FIFTH_SLOT);
        forecasterUnderTest.forecast(metricData);

        // Now the buffer is full, forecasts should match the data point values for same slot in previous cycle

        metricData = new MetricData(metricDef, SECOND_CYCLE_FIRST_SLOT_VALUE, SECOND_CYCLE_FIRST_SLOT);
        forecast = forecasterUnderTest.forecast(metricData);
        assertEquals(FIRST_CYCLE_FIRST_SLOT_VALUE, forecast.getValue(), TOLERANCE);

        // Next observation should be null (as 2nd slot of first cycle was missing)
        metricData = new MetricData(metricDef, SECOND_CYCLE_SECOND_SLOT_VALUE, SECOND_CYCLE_SECOND_SLOT);
        forecast = forecasterUnderTest.forecast(metricData);
        assertNull(forecast);
    }

    @Test
    public void testForecast_missingTwoDatapoints() {
        val params = new SeasonalNaivePointForecasterParams().setCycleLength(5).setIntervalLength(10);
        val forecasterUnderTest = new SeasonalNaivePointForecaster(params);

        MetricData metricData;
        PointForecast forecast;

        // Fill buffer with first cycle but with second and third slots missing

        metricData = new MetricData(metricDef, FIRST_CYCLE_FIRST_SLOT_VALUE, FIRST_CYCLE_FIRST_SLOT);
        forecasterUnderTest.forecast(metricData);

        // [Skip two datapoint here]

        metricData = new MetricData(metricDef, FIRST_CYCLE_FOURTH_SLOT_VALUE, FIRST_CYCLE_FOURTH_SLOT);
        forecasterUnderTest.forecast(metricData);

        metricData = new MetricData(metricDef, FIRST_CYCLE_FIFTH_SLOT_VALUE, FIRST_CYCLE_FIFTH_SLOT);
        forecasterUnderTest.forecast(metricData);

        // Now the buffer is full, forecasts should match the data point values for same slot in previous cycle (with nulls for missing):

        metricData = new MetricData(metricDef, SECOND_CYCLE_FIRST_SLOT_VALUE, SECOND_CYCLE_FIRST_SLOT);
        forecast = forecasterUnderTest.forecast(metricData);
        assertEquals(FIRST_CYCLE_FIRST_SLOT_VALUE, forecast.getValue(), TOLERANCE);

        metricData = new MetricData(metricDef, SECOND_CYCLE_SECOND_SLOT_VALUE, SECOND_CYCLE_SECOND_SLOT);
        forecast = forecasterUnderTest.forecast(metricData);
        assertNull(forecast);

        metricData = new MetricData(metricDef, SECOND_CYCLE_THIRD_SLOT_VALUE, SECOND_CYCLE_THIRD_SLOT);
        forecast = forecasterUnderTest.forecast(metricData);
        assertNull(forecast);

        metricData = new MetricData(metricDef, SECOND_CYCLE_FOURTH_SLOT_VALUE, SECOND_CYCLE_FOURTH_SLOT);
        forecast = forecasterUnderTest.forecast(metricData);
        assertEquals(FIRST_CYCLE_FOURTH_SLOT_VALUE, forecast.getValue(), TOLERANCE);

        metricData = new MetricData(metricDef, SECOND_CYCLE_FIFTH_SLOT_VALUE, SECOND_CYCLE_FIFTH_SLOT);
        forecast = forecasterUnderTest.forecast(metricData);
        assertEquals(FIRST_CYCLE_FIFTH_SLOT_VALUE, forecast.getValue(), TOLERANCE);
    }

    @Test
    public void testForecast_missingTwoDatapointsWithWrap() {
        val params = new SeasonalNaivePointForecasterParams().setCycleLength(5).setIntervalLength(10);
        val forecasterUnderTest = new SeasonalNaivePointForecaster(params);

        MetricData metricData;
        PointForecast forecast;

        // Fill buffer with last slot of first cycle missing and first slot of last cycle missing

        metricData = new MetricData(metricDef, FIRST_CYCLE_FIRST_SLOT_VALUE, FIRST_CYCLE_FIRST_SLOT);
        forecast = forecasterUnderTest.forecast(metricData);
        assertNull(forecast);
        // we miss one observation here
        metricData = new MetricData(metricDef, FIRST_CYCLE_SECOND_SLOT_VALUE, FIRST_CYCLE_SECOND_SLOT);
        forecast = forecasterUnderTest.forecast(metricData);
        assertNull(forecast);

        metricData = new MetricData(metricDef, FIRST_CYCLE_THIRD_SLOT_VALUE, FIRST_CYCLE_THIRD_SLOT);
        forecast = forecasterUnderTest.forecast(metricData);
        assertNull(forecast);

        metricData = new MetricData(metricDef, FIRST_CYCLE_FOURTH_SLOT_VALUE, FIRST_CYCLE_FOURTH_SLOT);
        forecast = forecasterUnderTest.forecast(metricData);
        assertNull(forecast);

        // [Skip both the last and first slot in the buffer]

        // Now do the forecast:

        metricData = new MetricData(metricDef, SECOND_CYCLE_SECOND_SLOT_VALUE, SECOND_CYCLE_SECOND_SLOT);
        forecast = forecasterUnderTest.forecast(metricData);
        assertEquals(FIRST_CYCLE_SECOND_SLOT_VALUE, forecast.getValue(), TOLERANCE);

        metricData = new MetricData(metricDef, SECOND_CYCLE_THIRD_SLOT_VALUE, SECOND_CYCLE_THIRD_SLOT);
        forecast = forecasterUnderTest.forecast(metricData);
        assertEquals(FIRST_CYCLE_THIRD_SLOT_VALUE, forecast.getValue(), TOLERANCE);

        metricData = new MetricData(metricDef, SECOND_CYCLE_FOURTH_SLOT_VALUE, SECOND_CYCLE_FOURTH_SLOT);
        forecast = forecasterUnderTest.forecast(metricData);
        assertEquals(FIRST_CYCLE_FOURTH_SLOT_VALUE, forecast.getValue(), TOLERANCE);

        // next observation is a null value
        metricData = new MetricData(metricDef, SECOND_CYCLE_FIRST_SLOT_VALUE, SECOND_CYCLE_FIFTH_SLOT);
        forecast = forecasterUnderTest.forecast(metricData);
        assertNull(forecast);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testForecast_nullMetricData() {
        new SeasonalNaivePointForecaster(new SeasonalNaivePointForecasterParams()).forecast(null);
    }
}
