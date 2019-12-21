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

import com.expedia.metrics.MetricData;
import com.expedia.metrics.MetricDefinition;
import lombok.val;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SeasonalBufferTest {
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
    private static final int VALID_CYCLE_LENGTH = 10;
    private static final int VALID_INTERVAL = 10;
    private static final double VALID_PLACEHOLDER = Double.POSITIVE_INFINITY;
    private static final MetricDefinition METRIC_DEF = new MetricDefinition("some-key");

    private SeasonalBuffer subject = new SeasonalBuffer(CYCLE_LENGTH, INTERVAL_LENGTH, VALID_PLACEHOLDER);


    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_nonPositiveCycleLength() {
        new SeasonalBuffer(0, VALID_INTERVAL, VALID_PLACEHOLDER);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_nonPositiveInterval() {
        new SeasonalBuffer(VALID_CYCLE_LENGTH, 0, VALID_PLACEHOLDER);
    }

    @Test
    public void testConstructor_nullPlaceholder() {
        val subjectWithNullPlaceholder = new SeasonalBuffer(VALID_CYCLE_LENGTH, VALID_INTERVAL, Double.NaN);
        updateAndCompare(subjectWithNullPlaceholder, FIRST_CYCLE_FIRST_SLOT, FIRST_CYCLE_FIRST_SLOT_VALUE, Double.NaN);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUpdate_nullMetricData() {
        subject.updateWhilePadding(null);
    }

    @Test(expected = MetricDeliveryTimeException.class)
    public void testUpdate_futureTimestamp() {
        long timestampFromPast = FIRST_CYCLE_FIRST_SLOT - 100;
        updateAndCompare(subject, FIRST_CYCLE_FIRST_SLOT, FIRST_CYCLE_FIRST_SLOT_VALUE, VALID_PLACEHOLDER);
        updateAndCompare(subject, timestampFromPast, FIRST_CYCLE_SECOND_SLOT_VALUE, VALID_PLACEHOLDER);
    }

    @Test(expected = MetricDeliveryDuplicateException.class)
    public void testUpdate_duplicateTimestamp() {
        updateAndCompare(subject, FIRST_CYCLE_FIRST_SLOT, FIRST_CYCLE_FIRST_SLOT_VALUE, VALID_PLACEHOLDER);
        updateAndCompare(subject, FIRST_CYCLE_FIRST_SLOT, FIRST_CYCLE_SECOND_SLOT_VALUE, VALID_PLACEHOLDER);
    }

    @Test
    public void testUpdate_allDatapointsPresent() {
        // First, initialize the buffer.
        updateAndCompare(subject, FIRST_CYCLE_FIRST_SLOT, FIRST_CYCLE_FIRST_SLOT_VALUE, VALID_PLACEHOLDER);
        updateAndCompare(subject, FIRST_CYCLE_SECOND_SLOT, FIRST_CYCLE_SECOND_SLOT_VALUE, VALID_PLACEHOLDER);
        updateAndCompare(subject, FIRST_CYCLE_THIRD_SLOT, FIRST_CYCLE_THIRD_SLOT_VALUE, VALID_PLACEHOLDER);
        updateAndCompare(subject, FIRST_CYCLE_FOURTH_SLOT, FIRST_CYCLE_FOURTH_SLOT_VALUE, VALID_PLACEHOLDER);
        updateAndCompare(subject, FIRST_CYCLE_FIFTH_SLOT, FIRST_CYCLE_FIFTH_SLOT_VALUE, VALID_PLACEHOLDER);

        // Now the buffer is full, we can forecast.
        updateAndCompare(subject, SECOND_CYCLE_FIRST_SLOT, SECOND_CYCLE_FIRST_SLOT_VALUE, FIRST_CYCLE_FIRST_SLOT_VALUE);
        updateAndCompare(subject, SECOND_CYCLE_SECOND_SLOT, SECOND_CYCLE_SECOND_SLOT_VALUE, FIRST_CYCLE_SECOND_SLOT_VALUE);
        updateAndCompare(subject, SECOND_CYCLE_THIRD_SLOT, SECOND_CYCLE_THIRD_SLOT_VALUE, FIRST_CYCLE_THIRD_SLOT_VALUE);
        updateAndCompare(subject, SECOND_CYCLE_FOURTH_SLOT, SECOND_CYCLE_FOURTH_SLOT_VALUE, FIRST_CYCLE_FOURTH_SLOT_VALUE);
        updateAndCompare(subject, SECOND_CYCLE_FIFTH_SLOT, SECOND_CYCLE_FIFTH_SLOT_VALUE, FIRST_CYCLE_FIFTH_SLOT_VALUE);
    }

    @Test
    public void testUpdate_missingOneDatapoint() {
        // Fill buffer with first cycle but with second slot missing
        updateAndCompare(subject, FIRST_CYCLE_FIRST_SLOT, FIRST_CYCLE_FIRST_SLOT_VALUE, VALID_PLACEHOLDER);
        // [Skip one datapoint here]
        updateAndCompare(subject, FIRST_CYCLE_THIRD_SLOT, FIRST_CYCLE_THIRD_SLOT_VALUE, VALID_PLACEHOLDER);
        updateAndCompare(subject, FIRST_CYCLE_FOURTH_SLOT, FIRST_CYCLE_FOURTH_SLOT_VALUE, VALID_PLACEHOLDER);
        updateAndCompare(subject, FIRST_CYCLE_FIFTH_SLOT, FIRST_CYCLE_FIFTH_SLOT_VALUE, VALID_PLACEHOLDER);

        // Now the buffer is full, forecasts should match the data point values for same slot in previous cycle
        updateAndCompare(subject, SECOND_CYCLE_FIRST_SLOT, SECOND_CYCLE_FIRST_SLOT_VALUE, FIRST_CYCLE_FIRST_SLOT_VALUE);
        // Next observation should be a placeholder (as 2nd slot of first cycle was missing)
        updateAndCompare(subject, SECOND_CYCLE_SECOND_SLOT, SECOND_CYCLE_SECOND_SLOT_VALUE, VALID_PLACEHOLDER);
        // Remaining observations should match original
        updateAndCompare(subject, SECOND_CYCLE_THIRD_SLOT, SECOND_CYCLE_THIRD_SLOT_VALUE, FIRST_CYCLE_THIRD_SLOT_VALUE);
        updateAndCompare(subject, SECOND_CYCLE_FOURTH_SLOT, SECOND_CYCLE_FOURTH_SLOT_VALUE, FIRST_CYCLE_FOURTH_SLOT_VALUE);
        updateAndCompare(subject, SECOND_CYCLE_FIFTH_SLOT, SECOND_CYCLE_FIFTH_SLOT_VALUE, FIRST_CYCLE_FIFTH_SLOT_VALUE);
    }

    @Test
    public void testUpdate_missingTwoDatapoints() {
        // Fill buffer with first cycle but with second slot missing
        updateAndCompare(subject, FIRST_CYCLE_FIRST_SLOT, FIRST_CYCLE_FIRST_SLOT_VALUE, VALID_PLACEHOLDER);
        // [Skip two datapoints here]
        updateAndCompare(subject, FIRST_CYCLE_FOURTH_SLOT, FIRST_CYCLE_FOURTH_SLOT_VALUE, VALID_PLACEHOLDER);
        updateAndCompare(subject, FIRST_CYCLE_FIFTH_SLOT, FIRST_CYCLE_FIFTH_SLOT_VALUE, VALID_PLACEHOLDER);

        // Now the buffer is full, forecasts should match the data point values for same slot in previous cycle
        updateAndCompare(subject, SECOND_CYCLE_FIRST_SLOT, SECOND_CYCLE_FIRST_SLOT_VALUE, FIRST_CYCLE_FIRST_SLOT_VALUE);
        // Next two observations should be a placeholder (as 2nd slot of first cycle was missing)
        updateAndCompare(subject, SECOND_CYCLE_SECOND_SLOT, SECOND_CYCLE_SECOND_SLOT_VALUE, VALID_PLACEHOLDER);
        updateAndCompare(subject, SECOND_CYCLE_THIRD_SLOT, SECOND_CYCLE_THIRD_SLOT_VALUE, VALID_PLACEHOLDER);
        // Remaining observations should match original
        updateAndCompare(subject, SECOND_CYCLE_FOURTH_SLOT, SECOND_CYCLE_FOURTH_SLOT_VALUE, FIRST_CYCLE_FOURTH_SLOT_VALUE);
        updateAndCompare(subject, SECOND_CYCLE_FIFTH_SLOT, SECOND_CYCLE_FIFTH_SLOT_VALUE, FIRST_CYCLE_FIFTH_SLOT_VALUE);
    }

    @Test
    public void testUpdate_missingTwoDatapointsWithWrap() {
        // Fill buffer with last slot of first cycle missing and first slot of second cycle missing
        updateAndCompare(subject, FIRST_CYCLE_FIRST_SLOT, FIRST_CYCLE_FIRST_SLOT_VALUE, VALID_PLACEHOLDER);
        updateAndCompare(subject, FIRST_CYCLE_SECOND_SLOT, FIRST_CYCLE_SECOND_SLOT_VALUE, VALID_PLACEHOLDER);
        updateAndCompare(subject, FIRST_CYCLE_THIRD_SLOT, FIRST_CYCLE_THIRD_SLOT_VALUE, VALID_PLACEHOLDER);
        updateAndCompare(subject, FIRST_CYCLE_FOURTH_SLOT, FIRST_CYCLE_FOURTH_SLOT_VALUE, VALID_PLACEHOLDER);
        // [Skip both the last and first slot in the buffer]

        updateAndCompare(subject, SECOND_CYCLE_SECOND_SLOT, SECOND_CYCLE_SECOND_SLOT_VALUE, FIRST_CYCLE_SECOND_SLOT_VALUE);
        updateAndCompare(subject, SECOND_CYCLE_THIRD_SLOT, SECOND_CYCLE_THIRD_SLOT_VALUE, FIRST_CYCLE_THIRD_SLOT_VALUE);
        updateAndCompare(subject, SECOND_CYCLE_FOURTH_SLOT, SECOND_CYCLE_FOURTH_SLOT_VALUE, FIRST_CYCLE_FOURTH_SLOT_VALUE);
        // Last slot of second cycle should be a placeholder as last slot of first cycle was skipped
        updateAndCompare(subject, SECOND_CYCLE_FIFTH_SLOT, SECOND_CYCLE_FIFTH_SLOT_VALUE, VALID_PLACEHOLDER);
        // First slot of third cycle should be a placeholder as first slot of second cycle was skipped
        updateAndCompare(subject, THIRD_CYCLE_FIRST_SLOT, THIRD_CYCLE_THIRD_SLOT_VALUE, VALID_PLACEHOLDER);
    }

    private void updateAndCompare(SeasonalBuffer subject, long timestamp, double newValue, double expectedOldValue) {
        MetricData metricData = new MetricData(METRIC_DEF, newValue, timestamp);
        updateAndCompare(subject, metricData, expectedOldValue);
    }

    private void updateAndCompare(SeasonalBuffer subject, MetricData metricData, double expectedOldValue) {
        val actualOldValue = subject.updateWhilePadding(metricData);
        assertEquals(expectedOldValue, actualOldValue, TOLERANCE);
    }
}
