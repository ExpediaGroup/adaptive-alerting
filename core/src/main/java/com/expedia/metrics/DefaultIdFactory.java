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

import java.util.*;

/**
 * Creates ids similar to the sample ids in the Metrics 2.0 specification.
 *
 * As the tags, keys, and values are sorted and joined with '=' and ',' with no
 * escaping this may create confusion if your keys or values contain '=' or
 * ','.
 */
public class DefaultIdFactory implements IdFactory {
    @Override
    public String getId(MetricDefinition metric) {
        final StringBuilder builder = new StringBuilder();
        if (metric.getKey() != null) {
            builder.append(metric.getKey())
                    .append(',');
        }
        final SortedMap<String, String> sortedKvTags = new TreeMap<>(metric.getTags().getKv());
        for (Map.Entry<String, String> entry : sortedKvTags.entrySet()) {
            builder.append(entry.getKey());
            if (entry.getValue() != null) {
                    builder.append('=')
                        .append(entry.getValue());
            }
            builder.append(',');
        }
        final SortedSet<String> sortedVTags = new TreeSet<>(metric.getTags().getV());
        for (String tag : sortedVTags) {
            builder.append(tag)
                    .append(',');
        }
        if (builder.length() > 0) {
            builder.deleteCharAt(builder.length() - 1);
        }
        return builder.toString();
    }
}
