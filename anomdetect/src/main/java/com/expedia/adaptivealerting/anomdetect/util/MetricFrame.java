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
package com.expedia.adaptivealerting.anomdetect.util;

import com.expedia.metrics.MetricData;

import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;

import static com.expedia.adaptivealerting.anomdetect.util.AssertUtil.isTrue;
import static com.expedia.adaptivealerting.anomdetect.util.AssertUtil.notNull;

/**
 * Data frame for time series metric data.
 */
public class MetricFrame {
    private final List<MetricData> metricDataPoints;

    /**
     * Creates a new metric frame from an array of {@link MetricData}s.
     *
     * @param metricDataPoints Metric point array.
     */
    public MetricFrame(MetricData[] metricDataPoints) {
        notNull(metricDataPoints, "metricData can't be null");
        this.metricDataPoints = Arrays.asList(metricDataPoints);
    }

    /**
     * Returns the number of metric points in the frame.
     *
     * @return Number of metric points in the frame.
     */
    public int getNumRows() {
        return metricDataPoints.size();
    }

    /**
     * Returns the {@link MetricData} at the given row index.
     *
     * @param index Row index.
     * @return The corresponding metric point.
     */
    public MetricData getMetricDataPoint(int index) {
        isTrue(index >= 0, "Required: index >= 0");
        return metricDataPoints.get(index);
    }

    public List<MetricData> getMetricData() {
        return metricDataPoints;
    }

    /**
     * Returns a list iterator for this frame. Intended to support data streaming.
     *
     * @return List iterator for this frame.
     */
    public ListIterator<MetricData> listIterator() {
        return metricDataPoints.listIterator();
    }
}
