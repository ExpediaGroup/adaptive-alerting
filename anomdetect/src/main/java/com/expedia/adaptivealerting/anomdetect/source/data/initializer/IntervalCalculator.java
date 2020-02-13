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
package com.expedia.adaptivealerting.anomdetect.source.data.initializer;

import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import com.expedia.adaptivealerting.anomdetect.detect.MappedMetricData;

@Data
@Accessors(chain = true)
@Slf4j
public class IntervalCalculator {

    private long earliestTime;
    private long latestTime;

    public IntervalCalculator(long currentMetricTimestamp, long intervalLength, long cycleLength) {
        this.latestTime = calculateLatestTime(currentMetricTimestamp, intervalLength);
        this.earliestTime = calculateEarliestTime(intervalLength, cycleLength, this.latestTime);
    }

    private long calculateEarliestTime(long intervalLength, long cycleLength, long latestTime) {
        val fullWindow = cycleLength * intervalLength;
        return latestTime - fullWindow;
    }

    private long calculateLatestTime(long currentMetricTimestamp, long intervalLength) {
        return (currentMetricTimestamp / intervalLength) * intervalLength;
    }
}
