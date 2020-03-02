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
package com.expedia.adaptivealerting.anomdetect.source.data.initializer.reconstructbuffer;

import com.expedia.adaptivealerting.anomdetect.detect.MappedMetricData;
import com.expedia.adaptivealerting.anomdetect.source.data.DataSourceResult;
import lombok.val;

import java.util.ArrayList;
import java.util.List;

public class SeasonalBufferSynthesizer {

    private static final int A_WEEK_IN_SECONDS = 60 * 60 * 24 * 7;
    private static final int THREE_DAYS_IN_SECONDS = 259200;
    private static final int A_MINUTE_IN_SECONDS = 60;

    private final int interval = 60;  // TODO (in seconds) load in constructor

    public SeasonalBufferSynthesizer(MappedMetricData mappedMetricData) {
        // Initialise interval from mappedMetricData here
    }

    public List<DataSourceResult> reconstructSeasonalBuffer(
            List<Double> scaleFactor,
            List<DataSourceResult> aDayFromGraphite) {
        List<DataSourceResult> buffer = new ArrayList<>();
        // TODO reconstruct from
        return buffer;
    }

    private long getWeekOffset(long timestampUnix) {
        // Calculate time in minutes since start of this week
        // Deduct three days as timestamp 0 started on Thursday
        long mondayMidnight = (timestampUnix - THREE_DAYS_IN_SECONDS) - timestampUnix % A_WEEK_IN_SECONDS;
        long offset = timestampUnix - mondayMidnight;
        // Offset is in seconds, convert to minutes
        return (long) (offset / A_MINUTE_IN_SECONDS);
    }

    private List<Double> transformScaleFactor(List<Double> scaleFactor, int interval, int bufferSize) {
        // Make coefficients by repeating each scale factor interval (in minutes) times
        List<Double> coefficients = new ArrayList<>(bufferSize);
        for (int index = 0; index < bufferSize; index++) {
            coefficients.set(index, scaleFactor.get(index / interval));
        }
        return coefficients;
    }

    private List<DataSourceResult> reconstructSeasonalBuffer(
            List<DataSourceResult> metricSampleData,
            List<Double> coefficients,
            int bufferSize) {

        // Reconstruct seasonal buffer using metrics from one day and coefficients
        long startTime = metricSampleData.get(0).getEpochSecond();
        long offset = getWeekOffset(startTime);
        List<DataSourceResult> seasonalBuffer = new ArrayList<>(bufferSize);
        for (int i = 0; i < bufferSize; i++) {
            int seasonalBufferOffset = (int) ((offset + i) % bufferSize);
            val pointValue = metricSampleData.get(i % 1440).getDataPoint();
            val coefficient = coefficients.get(seasonalBufferOffset);
            val reconstructedValue = pointValue * coefficient;
            val reconstructedTime = startTime + i * this.interval;
            val newDatapoint = new DataSourceResult(reconstructedValue, reconstructedTime);
            seasonalBuffer.set(seasonalBufferOffset, newDatapoint);
        }
        return seasonalBuffer;
    }
}
