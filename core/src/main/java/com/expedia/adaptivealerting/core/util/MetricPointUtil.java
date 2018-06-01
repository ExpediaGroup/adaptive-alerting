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

import com.expedia.adaptivealerting.core.OutlierLevel;
import com.expedia.www.haystack.commons.entities.MetricPoint;
import com.expedia.www.haystack.commons.entities.MetricType;
import scala.Enumeration;
import scala.collection.immutable.Map;
import scala.collection.immutable.Map$;

import java.time.Instant;

/**
 * {@link MetricPoint} utilities.
 *
 * @author Willie Wheeler
 */
public final class MetricPointUtil {
    public static final String OUTLIER_LEVEL_TAG_NAME = "outlierLevel";
    
    private static final Enumeration.Value DEFAULT_TYPE = MetricType.Gauge();
    private static final Map<String, String> DEFAULT_TAGS = Map$.MODULE$.<String, String>empty();
    
    /**
     * Prevent instantiation.
     */
    private MetricPointUtil() {
    }
    
    /**
     * {@link MetricPoint} factory method. Metric points have name "data", type gauge and no tags.
     *
     * @param instant Metric instant.
     * @param value   Metric value.
     * @return Metric point.
     */
    public static MetricPoint metricPoint(Instant instant, float value) {
        return metricPoint("data", instant, value);
    }
    
    /**
     * {@link MetricPoint} factory method. Metric points have type gauge and no tags.
     *
     * @param name    Metric name.
     * @param instant Metric instant.
     * @param value   Metric value.
     * @return Metric point.
     */
    public static MetricPoint metricPoint(String name, Instant instant, float value) {
        return new MetricPoint(name, DEFAULT_TYPE, DEFAULT_TAGS, value, instant.getEpochSecond());
    }
    
    /**
     * <p>
     * Copies the given metric point and enriches the copy with its classification.
     * </p>
     * <p>
     * We'll probably move this into an abstract base class for outlier detectors, but I want to chat with the team
     * first.
     * </p>
     *
     * @param metricPoint  Metric point.
     * @param outlierLevel Outlier level.
     * @return A new metric tagged with the outlier level (tag is "outlierLevel").
     */
    public static MetricPoint classify(MetricPoint metricPoint, OutlierLevel outlierLevel) {
        final Map tags = metricPoint.tags().updated(OUTLIER_LEVEL_TAG_NAME, outlierLevel.name());
        return new MetricPoint(
                metricPoint.metric(),
                metricPoint.type(),
                tags,
                metricPoint.value(),
                metricPoint.epochTimeInSeconds());
    }
}
