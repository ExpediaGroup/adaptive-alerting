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
package com.expedia.adaptivealerting.anomdetect.testutil;

import com.expedia.metrics.MetricData;
import com.expedia.metrics.MetricDefinition;

import static com.expedia.adaptivealerting.anomdetect.testutil.DateHelper.timestamp;
import static com.expedia.metrics.TagCollection.EMPTY;

public class MetricDataHelper {
    public static final long NINE_AM_TIMESTAMP = timestamp(2020, 1, 1, 9, 0, 0);
    public static final long EIGHT_AM_TIMESTAMP = timestamp(2020, 1, 1, 8, 0, 0);

    public static MetricData buildMetricDataInsideNineToFive(double value) {
        return buildMetricData(value, NINE_AM_TIMESTAMP);
    }

    public static MetricData buildMetricDataOutsideNineToFive(double value) {
        return buildMetricData(value, EIGHT_AM_TIMESTAMP);
    }

    public static MetricData buildMetricData(double value, long nineAmTimestamp) {
        return new MetricData(new MetricDefinition(EMPTY, EMPTY), value, nineAmTimestamp);
    }
}
