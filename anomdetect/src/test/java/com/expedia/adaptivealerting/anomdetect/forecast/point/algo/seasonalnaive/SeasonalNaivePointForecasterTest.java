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

import static org.junit.Assert.*;
import static org.junit.Assert.assertFalse;

public class SeasonalNaivePointForecasterTest {
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
    private static final long THIRD_CYCLE_FIRST_SLOT = FIRST_CYCLE_FIRST_SLOT + (INTERVAL_LENGTH * 10);
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
    private static final double THIRD_CYCLE_THIRD_SLOT_VALUE = 110.0;
    private static final double VALID_PLACEHOLDER = Double.POSITIVE_INFINITY;
    private static final SeasonalNaivePointForecasterParams PARAMS =
            new SeasonalNaivePointForecasterParams().setCycleLength(CYCLE_LENGTH).setIntervalLength(INTERVAL_LENGTH);
    private static final MetricDefinition METRIC_DEF = new MetricDefinition("some-key");

    private SeasonalNaivePointForecaster subject = new SeasonalNaivePointForecaster(PARAMS);

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_notNull() {
        new SeasonalNaivePointForecaster(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testForecast_nullMetricData() {
        subject.forecast(null);
    }

    @Test
    public void testForecast_isWarmupPeriod() {
        forecastAndExpectWarmup(subject, FIRST_CYCLE_FIRST_SLOT, FIRST_CYCLE_FIRST_SLOT_VALUE);
        forecastAndExpectWarmup(subject, FIRST_CYCLE_SECOND_SLOT, FIRST_CYCLE_SECOND_SLOT_VALUE);
    }

    @Test
    public void testForecast_warmupPeriodPassedExpectAnomalyResult() {
        // Fill buffer
        forecastAndExpectWarmup(subject, FIRST_CYCLE_FIRST_SLOT, FIRST_CYCLE_FIRST_SLOT_VALUE);
        forecastAndExpectWarmup(subject, FIRST_CYCLE_SECOND_SLOT, FIRST_CYCLE_SECOND_SLOT_VALUE);
        forecastAndExpectWarmup(subject, FIRST_CYCLE_THIRD_SLOT, FIRST_CYCLE_THIRD_SLOT_VALUE);
        forecastAndExpectWarmup(subject, FIRST_CYCLE_FOURTH_SLOT, FIRST_CYCLE_FOURTH_SLOT_VALUE);
        forecastAndExpectWarmup(subject, FIRST_CYCLE_FIFTH_SLOT, FIRST_CYCLE_FIFTH_SLOT_VALUE);

        // Buffer is full, warmup period has ended
        forecastAndExpectNotWarmup(subject, SECOND_CYCLE_FIRST_SLOT, SECOND_CYCLE_FIRST_SLOT_VALUE);
    }

    @Test
    public void testForecast_missingTwoDatapointsWithWrap() {
        // Fill buffer with last slot of first cycle missing and first slot of second cycle missing
        forecastAndExpectWarmup(subject, FIRST_CYCLE_FIRST_SLOT, FIRST_CYCLE_FIRST_SLOT_VALUE);
        forecastAndExpectWarmup(subject, FIRST_CYCLE_SECOND_SLOT, FIRST_CYCLE_SECOND_SLOT_VALUE);
        forecastAndExpectWarmup(subject, FIRST_CYCLE_THIRD_SLOT, FIRST_CYCLE_THIRD_SLOT_VALUE);
        forecastAndExpectWarmup(subject, FIRST_CYCLE_FOURTH_SLOT, FIRST_CYCLE_FOURTH_SLOT_VALUE);

        // [Skip both the last and first slot in the buffer]

        forecastAndCompare(subject, SECOND_CYCLE_SECOND_SLOT, SECOND_CYCLE_SECOND_SLOT_VALUE, FIRST_CYCLE_SECOND_SLOT_VALUE);
        forecastAndCompare(subject, SECOND_CYCLE_THIRD_SLOT, SECOND_CYCLE_THIRD_SLOT_VALUE, FIRST_CYCLE_THIRD_SLOT_VALUE);
        forecastAndCompare(subject, SECOND_CYCLE_FOURTH_SLOT, SECOND_CYCLE_FOURTH_SLOT_VALUE, FIRST_CYCLE_FOURTH_SLOT_VALUE);
        // Last slot of second cycle should be a placeholder as last slot of first cycle was skipped
        forecastAndExpectNull(subject, SECOND_CYCLE_FIFTH_SLOT, SECOND_CYCLE_FIFTH_SLOT_VALUE);

        // First slot of third cycle should be a placeholder as first slot of second cycle was skipped
        forecastAndExpectNull(subject, THIRD_CYCLE_FIRST_SLOT, THIRD_CYCLE_THIRD_SLOT_VALUE);
    }

    private void forecastAndExpectNull(SeasonalNaivePointForecaster subject, long timestamp, double newValue) {
        MetricData metricData = new MetricData(METRIC_DEF, newValue, timestamp);
        assertNull(subject.forecast(metricData));
    }

    private void forecastAndCompare(SeasonalNaivePointForecaster subject, long timestamp, double newValue, double expectedForecastValue) {
        MetricData metricData = new MetricData(METRIC_DEF, newValue, timestamp);
        val actualForecast = subject.forecast(metricData);
        assertEquals(new PointForecast(expectedForecastValue, false), actualForecast);
    }

    private void forecastAndExpectWarmup(SeasonalNaivePointForecaster subject, long timestamp, double newValue) {
        MetricData metricData = new MetricData(METRIC_DEF, newValue, timestamp);
        val forecast = subject.forecast(metricData);
        assertTrue(forecast.isWarmup());

    }

    private void forecastAndExpectNotWarmup(SeasonalNaivePointForecaster subject, long timestamp, double newValue) {
        MetricData metricData = new MetricData(METRIC_DEF, newValue, timestamp);
        val forecast = subject.forecast(metricData);
        assertFalse(forecast.isWarmup());
    }

}
