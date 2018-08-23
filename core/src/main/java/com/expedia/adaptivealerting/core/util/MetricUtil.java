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
package com.expedia.adaptivealerting.core.util;

import com.expedia.adaptivealerting.core.data.MetricFrame;
import com.expedia.adaptivealerting.core.data.Mpoint;
import com.expedia.metrics.MetricDefinition;
import com.expedia.www.haystack.commons.entities.MetricPoint;
import com.expedia.www.haystack.commons.entities.MetricType;
import scala.Enumeration;
import scala.collection.immutable.Map;
import scala.collection.immutable.Map$;

import java.util.ArrayList;
import java.util.List;

import static com.expedia.adaptivealerting.core.util.AssertUtil.notNull;

/**
 * {@link MetricPoint} utilities.
 *
 * @author Willie Wheeler
 */
public final class MetricUtil {
    private static final Enumeration.Value DEFAULT_TYPE = MetricType.Gauge();
    private static final Map<String, String> DEFAULT_TAGS = Map$.MODULE$.<String, String>empty();
    
    /**
     * Prevent instantiation.
     */
    private MetricUtil() {
    }
    
    /**
     * {@link MetricPoint} factory method. Metric points have name "data", type gauge and no tags.
     *
     * @param epochSecond Epoch time in seconds.
     * @param value       Metric value.
     * @return Metric point.
     */
    public static MetricPoint metricPoint(long epochSecond, double value) {
        return metricPoint("data", epochSecond, value);
    }
    
    /**
     * {@link MetricPoint} factory method. Metric points have type gauge and no tags.
     *
     * @param name        Metric name.
     * @param epochSecond Epoch time in seconds.
     * @param value       Metric value.
     * @return Metric point.
     */
    public static MetricPoint metricPoint(String name, long epochSecond, double value) {
        return new MetricPoint(name, DEFAULT_TYPE, DEFAULT_TAGS, (float) value, epochSecond);
    }
    
    /**
     * Convert {@link MetricPoint} to a {@link Mpoint}.
     *
     * @param metricPoint a metric point.
     * @return an Mpoint.
     */
    public static Mpoint toMpoint(MetricPoint metricPoint) {
        final Mpoint mpoint = new Mpoint();
        mpoint.setMetricDefinition(toMetric(metricPoint));
        mpoint.setEpochTimeInSeconds(metricPoint.epochTimeInSeconds());
        mpoint.setValue(metricPoint.value());
        return mpoint;
    }
    
    public static MetricFrame merge(List<MetricFrame> frames) {
        notNull(frames, "frames can't be null");
        
        int totalSize = 0;
        for (final MetricFrame frame : frames) {
            totalSize += frame.getNumRows();
        }
        
        final List<Mpoint> resultList = new ArrayList<>(totalSize);
        for (final MetricFrame frame : frames) {
            resultList.addAll(frame.getMetricPoints());
        }
        
        return new MetricFrame(resultList);
    }
    
    private static MetricDefinition toMetric(MetricPoint metricPoint) {
        MetricDefinition metric = new MetricDefinition();
        metric.addTags(scala.collection.JavaConverters
                .mapAsJavaMapConverter(metricPoint.tags()).asJava());
        return metric;
    }
}
