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
import com.expedia.adaptivealerting.anomdetect.forecast.point.PointForecaster;
import com.expedia.metrics.MetricData;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.util.UUID;
import java.util.stream.IntStream;

import static com.expedia.adaptivealerting.anomdetect.util.AssertUtil.notNull;

/**
 * Point forecaster based on the seasonal naive method described in
 * https://otexts.com/fpp2/simple-methods.html#simple-methods.
 */
@RequiredArgsConstructor
public class SeasonalNaivePointForecaster implements PointForecaster {

    private static final long NOT_YET_INITIALIZED = -1L;

    /**
     * Detector UUID.
     */
    @NonNull
    @Getter
    private UUID uuid;

    /**
     * Configuration parameters for the {@link SeasonalNaivePointForecaster}.
     */
    @NonNull
    @Getter
    private SeasonalNaivePointForecasterParams params;

    /**
     * Buffer holding {@link SeasonalNaivePointForecasterParams#getCycleLength()} data points.
     */
    private Double[] buffer;

    /**
     * Current index for the buffer.
     */
    private int currIndex;

    /**
     * Timestamp of the last data point.
     */
    private long lastTimestamp;

    /**
     * Creates a new forecaster from the given configuration parameters.
     * @param params Configuration parameters.
     */
    public SeasonalNaivePointForecaster(SeasonalNaivePointForecasterParams params) {
        notNull(params, "params can't be null");
        params.validate();
        this.params = params;
        initState();
    }

    /**
     * Sets up the initial state.
     */
    private void initState() {
        val n = this.params.getCycleLength();
        this.buffer = new Double[n];
        this.currIndex = 0;
        lastTimestamp = NOT_YET_INITIALIZED;
    }

    /**
     * Forecasts the data point value for the given data point using seasonal naive algorithm.
     * @param metricData The data point.
     * @return Forecast for the given data point.
     */
    @Override
    public PointForecast forecast(MetricData metricData) {
        notNull(metricData, "metricData can't be null");

        handleMissingDataPoints(metricData);
        val currForecastValue = this.buffer[currIndex];
        updateBuffer(metricData);

        return currForecastValue == null ? null : new PointForecast(currForecastValue, false);
    }

    /**
     * Moves currIndex for the number of missing data points and fills blanks with null values.
     * @param metricData The new data point.
     */
    private void handleMissingDataPoints(MetricData metricData) {
        if (isFirstDataPoint()) {
            // This is the first metric value we've received, therefore there are no missing data points prior to this one.
            return;
        }
        // Find number of missing data points based on the last timestamp and
        // the metrics interval.
        int timeDifference = new Long(metricData.getTimestamp() - lastTimestamp).intValue();
        int intervalLength = this.params.getIntervalLength();
        val missingDataPointsCount = timeDifference / intervalLength - 1;

        // Fill data points between last datapoint timestamp and current data point timestamp
        // with null values.
        IntStream.range(0, missingDataPointsCount).forEach( __ -> {
            buffer[currIndex] = null;
            currIndex = (currIndex + 1) % this.buffer.length;
        });
    }

    /**
     * Is this the first time the forecaster has been used?
     * 
     * @return false if we have set lastTimestamp at least once before
     */
    private boolean isFirstDataPoint() {
        return lastTimestamp == NOT_YET_INITIALIZED;
    }

    /**
     * Updates buffer with the new data point for the next forecasting cycle.
     * @param metricData Data point to update buffer with.
     */
    private void updateBuffer(MetricData metricData) {
        this.buffer[currIndex] = metricData.getValue();
        this.currIndex = (this.currIndex + 1) % this.buffer.length;
        this.lastTimestamp = metricData.getTimestamp();
    }
}
