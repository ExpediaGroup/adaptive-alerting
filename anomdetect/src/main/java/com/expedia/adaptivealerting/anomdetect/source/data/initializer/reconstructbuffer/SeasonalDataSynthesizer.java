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

import com.expedia.adaptivealerting.anomdetect.source.data.DataSourceResult;
import com.expedia.adaptivealerting.anomdetect.util.TimeConstantsUtil;
import lombok.NoArgsConstructor;
import lombok.val;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
public class SeasonalDataSynthesizer {

    private static final int FIVE_MINUTE_INTERVALS_IN_A_WEEK = 12 * 24 * 7;

    ReconstructParameters parameters;

    public List<DataSourceResult> reconstructSeasonalData(
                List<DataSourceResult> sampleData,
                List<Double> scaleFactor,
                ReconstructParameters parameters) {
        this.parameters = parameters;
        return reconstructSeasonalData(sampleData, scaleFactor);
    }

    private List<DataSourceResult> reconstructSeasonalData(
                List<DataSourceResult> sampleData,
                List<Double> coefficients) {

        // Reconstruct seasonal buffer using metrics from one day and coefficients
        long startTime = sampleData.get(0).getEpochSecond();
        long offset = getWeekOffset(startTime);
        val bufferSize = this.parameters.getTimeSeriesSize();
        val intervalLength = this.parameters.getTimeSeriesInterval();
        List<DataSourceResult> seasonalBuffer = new ArrayList<>(bufferSize);
        for (int i = 0; i < bufferSize; i++) {
            int seasonalBufferOffset = (int) ((offset + i) % bufferSize);
            val pointValue = sampleData.get(i % FIVE_MINUTE_INTERVALS_IN_A_WEEK).getDataPoint();
            val coefficient = coefficients.get(seasonalBufferOffset);
            val reconstructedValue = pointValue * coefficient;
            val reconstructedTime = startTime + i * intervalLength;
            val dataPoint = new DataSourceResult(reconstructedValue, reconstructedTime);
            seasonalBuffer.set(seasonalBufferOffset, dataPoint);
        }
        return seasonalBuffer;
    }

    private long getWeekOffset(long timestampUnix) {
        // Calculate time in minutes since start of this week
        // Deduct three days as timestamp 0 started on Thursday
        long mondayMidnight = (timestampUnix - 3 * TimeConstantsUtil.SECONDS_PER_DAY) - timestampUnix % TimeConstantsUtil.SECONDS_PER_WEEK;
        long offset = timestampUnix - mondayMidnight;
        // Offset is in seconds, convert to minutes
        return (long) (offset / TimeConstantsUtil.SECONDS_PER_MIN);
    }

    private List<Double> transformScaleFactor(List<Double> scaleFactor, int interval, int bufferSize) {
        // Make coefficients by repeating each scale factor interval (in minutes) times
        List<Double> coefficients = new ArrayList<>(bufferSize);
        for (int index = 0; index < bufferSize; index++) {
            coefficients.set(index, scaleFactor.get(index / interval));
        }
        return coefficients;
    }
}
