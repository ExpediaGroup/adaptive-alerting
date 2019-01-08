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
package com.expedia.adaptivealerting.tools.pipeline.source;

import com.expedia.adaptivealerting.core.data.MetricFrame;
import com.expedia.metrics.MetricData;

import java.util.ListIterator;

/**
 * Metric source backed by a {@link MetricFrame}.
 *
 * @author Willie Wheeler
 */
public final class MetricFrameMetricSource extends AbstractMetricSource {
    private ListIterator<MetricData> metricDataIterator;
    
    /**
     * Publishes data from the given metric frame to any subscribers.
     *
     * @param metricFrame Source metric frame.
     * @param metricName  Metric name.
     * @param periodMs    Publication period in milliseconds.
     */
    public MetricFrameMetricSource(MetricFrame metricFrame, String metricName, long periodMs) {
        super(metricName, periodMs);
        this.metricDataIterator = metricFrame.listIterator();
    }
    
    @Override
    public MetricData next() {
        if (metricDataIterator.hasNext()) {
            return metricDataIterator.next();
        } else {
            return null;
        }
    }
}
