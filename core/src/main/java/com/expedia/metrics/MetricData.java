/*
 * Copyright 2018 Expedia Group, Inc.
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
package com.expedia.metrics;

import java.util.Objects;

public class MetricData {
    private final MetricDefinition metricDefinition;
    private final double value;
    private final long timestamp;

    public MetricData(MetricDefinition metricDefinition, double value, long timestamp) {
        if (metricDefinition == null) {
            throw new IllegalArgumentException("metricDefinition is required");
        }
        this.metricDefinition = metricDefinition;
        this.value = value;
        this.timestamp = timestamp;
    }

    public MetricDefinition getMetricDefinition() {
        return metricDefinition;
    }

    public double getValue() {
        return value;
    }

    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MetricData that = (MetricData) o;
        return Objects.equals(metricDefinition, that.metricDefinition) &&
                value == that.value &&
                timestamp == that.timestamp;
    }

    @Override
    public int hashCode() {
        return metricDefinition.hashCode() ^
                31 * Double.hashCode(value) ^
                17 * Long.hashCode(timestamp);
    }

    @Override
    public String toString() {
        return "MetricData{" +
                "metricDefinition=" + metricDefinition +
                ", value=" + value +
                ", timestamp=" + timestamp +
                '}';
    }
}
