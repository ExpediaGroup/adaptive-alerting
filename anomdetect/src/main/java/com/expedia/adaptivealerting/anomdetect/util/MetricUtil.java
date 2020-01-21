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

import com.expedia.adaptivealerting.anomdetect.detect.MappedMetricData;
import com.expedia.metrics.MetricData;
import com.expedia.metrics.MetricDefinition;
import com.expedia.metrics.TagCollection;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.expedia.adaptivealerting.anomdetect.util.AssertUtil.notNull;

/**
 * Metric utilities.
 */
@UtilityClass
@Slf4j
public class MetricUtil {

    public static final String DATA_RETRIEVAL_TAG_KEY = "graphite.functionTagKey";

    public static Map<String, String> defaultKvTags() {
        val kvTags = new HashMap<String, String>();
        kvTags.put(MetricDefinition.UNIT, "");
        kvTags.put(MetricDefinition.MTYPE, "gauge");
        return kvTags;
    }

    public static Set<String> defaultVTags() {
        return new HashSet<>();
    }

    public static MetricDefinition metricDefinition() {
        return new MetricDefinition(new TagCollection(defaultKvTags(), defaultVTags()));
    }

    /**
     * Convenience method to create a new metric definition from the given tags. Provides defaults for null values.
     *
     * @param kvTags Key/value tags. Sets unit=[empty string] and mtype=gauge if null.
     * @param vTags  Value tags. Uses empty set if null.
     * @return Metric definition.
     */
    public static MetricDefinition metricDefinition(Map<String, String> kvTags, Set<String> vTags) {
        if (kvTags == null) {
            kvTags = defaultKvTags();
        }
        if (vTags == null) {
            vTags = defaultVTags();
        }
        return new MetricDefinition(new TagCollection(kvTags, vTags));
    }

    public static MetricData metricData(MetricDefinition metricDef) {
        notNull(metricDef, "metricDef can't be null");
        return metricData(metricDef, 0.0);
    }

    /**
     * Convenience method to create a new {@link MetricData} from the given definition and value. Sets the timestamp to
     * the current epoch second.
     *
     * @param metricDef Metric definition.
     * @param value     Value.
     * @return Metric data.
     */
    public static MetricData metricData(MetricDefinition metricDef, double value) {
        return new MetricData(metricDef, value, Instant.now().getEpochSecond());
    }

    /**
     * Convenience method to create a new {@link MetricData} from the given definition, value and epoch second.
     *
     * @param metricDef   Metric definition.
     * @param value       Value.
     * @param epochSecond Epoch second.
     * @return Metric data.
     */
    public static MetricData metricData(MetricDefinition metricDef, double value, long epochSecond) {
        return new MetricData(metricDef, value, epochSecond);
    }

    /**
     * Convenience method to get the value of the tag that has a key matching DATA_RETRIEVAL_TAG_KEY for the provided metric.
     * If the provided metric does not contain that tag, the key of the metric's MetricDefinition will be returned.
     *
     * @param mappedMetricData Mapped metric data
     * @return Returns value for the provided data retrieval tag key. If value is not present then the key of the metric's MetricDefinition will be returned.
     */
    public static String getDataRetrievalValueOrMetricKey(MappedMetricData mappedMetricData) {
        val metricData = mappedMetricData.getMetricData();
        val metricDefinition = metricData.getMetricDefinition();
        val metricTags = metricDefinition.getTags();
        val dataRetrievalTagKey = PropertiesUtil.getValueFromProperty(DATA_RETRIEVAL_TAG_KEY);
        val metricKey = metricDefinition.getKey();

        if (dataRetrievalTagKey == null) {
            return metricKey;
        }

        val dataRetrievalTagValue = getValueFromTagKey(metricTags, dataRetrievalTagKey);
        if (dataRetrievalTagValue != null) {
            return dataRetrievalTagValue;
        } else {
            log.warn("Provided data retrieval key={} doesn't exist. Returning metric key instead", dataRetrievalTagKey);
            return metricKey;
        }
    }

    private String getValueFromTagKey(TagCollection metricTags, String dataRetrievalTagKey) {
        return metricTags != null && metricTags.getKv() != null
                ? metricTags.getKv().get(dataRetrievalTagKey) : null;
    }
}
