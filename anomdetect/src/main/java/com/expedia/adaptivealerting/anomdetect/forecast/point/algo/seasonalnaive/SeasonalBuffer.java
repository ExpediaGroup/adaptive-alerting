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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.stream.IntStream;

import static com.expedia.adaptivealerting.anomdetect.util.AssertUtil.isStrictlyPositive;
import static com.expedia.adaptivealerting.anomdetect.util.AssertUtil.notNull;

/**
 * In-memory buffer. Stores historical values for a seasonal detector.
 */
@RequiredArgsConstructor
@Slf4j
public class SeasonalBuffer {

    public static final double DEFAULT_MISSING_VALUE = Double.NEGATIVE_INFINITY;
    private static final long NOT_YET_INITIALIZED = -1L;

    /**
     * Number of observations per cycle.
     */
    private int cycleLength;

    /**
     * Number of seconds between two observations.
     */
    private int interval;

    /**
     * Value to use to represent a missing datapoint.
     */
    private Double missingValuePlaceholder;

    /**
     * Buffer holding {@link SeasonalNaivePointForecasterParams#getCycleLength()} datapoints.
     */
    private Double[] buffer;

    /**
     * Current index for the buffer.
     */
    private int currIndex;

    /**
     * Timestamp of the last datapoint.
     */
    private long lastTimestamp;

    public SeasonalBuffer(int cycleLength, int interval, Double missingValuePlaceholder) {
        isStrictlyPositive(cycleLength, "Required: cycleLength > 0");
        isStrictlyPositive(interval, "Required: interval > 0");
        notNull(interval, "Required: missingValue");
        this.cycleLength = cycleLength;
        this.interval = interval;
        this.missingValuePlaceholder = missingValuePlaceholder;
        initState();
    }

    private void initState() {
        this.lastTimestamp = NOT_YET_INITIALIZED;
        this.buffer = new Double[this.cycleLength];
        Arrays.fill(this.buffer, this.missingValuePlaceholder);
        this.currIndex = 0;
    }

    /**
     * Moves currIndex for the number of missing datapoints and fills blanks with MISSING_VALUE values.
     *
     * @param metricData The new datapoint.
     */
    public void padMissingDataPoints(MetricData metricData) {
        if (isFirstDataPoint()) return; // This is first metric value received. Assume it starts the cycle (i.e. no prior datapoints to pad)
        if (!checkValidTimestamp(metricData)) return;
        int numSkippedDataPoints = countIntervalsSkippedSinceLastTimestamp(metricData);
        insertSkippedDataPoints(numSkippedDataPoints);
    }

    /**
     * Updates buffer with the new datapoint.
     *
     * @param metricData Datapoint to update buffer with.
     */
    public void updateBuffer(MetricData metricData) {
        this.buffer[currIndex] = metricData.getValue();
        this.currIndex = (this.currIndex + 1) % this.buffer.length;
        this.lastTimestamp = metricData.getTimestamp();
    }

    public boolean isValueForCurrentIndexMissing() {
        return getValueForCurrentIndex() == DEFAULT_MISSING_VALUE;
    }

    public double getValueForCurrentIndex() {
        return this.buffer[currIndex];
    }

    /**
     * Fill datapoints between last datapoint timestamp and current datapoint timestamp with MISSING_VALUE values.
     */
    private void insertSkippedDataPoints(int numSkippedDataPoints) {
        IntStream.range(0, numSkippedDataPoints).forEach(__ -> {
            buffer[currIndex] = this.missingValuePlaceholder;
            currIndex = (currIndex + 1) % this.buffer.length;
        });
    }

    /**
     * Find number of missing datapoints based on the last timestamp and interval.
     */
    private int countIntervalsSkippedSinceLastTimestamp(MetricData metricData) {
        int timeDifference = new Long(metricData.getTimestamp() - lastTimestamp).intValue();
        return timeDifference / this.interval - 1;
    }

    private boolean checkValidTimestamp(MetricData metricData) {
        if (metricData.getTimestamp() < lastTimestamp) {
            log.warn(String.format("%s arrived after a metric with future timestamp %d", metricData, lastTimestamp));
            return false;
        }
        return true;
    }

    /**
     * Is this the first time the forecaster has been used?
     *
     * @return false if we have set lastTimestamp at least once before
     */
    private boolean isFirstDataPoint() {
        return lastTimestamp == NOT_YET_INITIALIZED;
    }
}
