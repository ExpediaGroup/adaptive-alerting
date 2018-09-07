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
package com.expedia.adaptivealerting.anomdetect;

import com.expedia.adaptivealerting.core.data.Metric;

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * @author Peter Hall
 * @deprecated Use real metric key factory when it's available
 */
public class TempMetricKeyFactory {
    
    public String toKey(Metric metric) {
        final SortedMap<String, String> sortedTags = new TreeMap<>(metric.getTags());
        final StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, String> entry : sortedTags.entrySet()) {
            builder.append(entry.getKey());
            if (entry.getValue() != null) {
                builder.append('=')
                        .append(entry.getValue());
            }
            builder.append(',');
        }
        if (builder.length() > 0) {
            builder.deleteCharAt(builder.length() - 1);
        }
        return builder.toString();
    }
}
