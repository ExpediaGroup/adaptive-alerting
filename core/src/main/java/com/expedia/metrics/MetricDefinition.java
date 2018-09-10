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

public class MetricDefinition {
    public static final String UNIT = "unit";
    public static final String MTYPE = "mtype";

    private static final List<String> REQUIRED_TAGS = Arrays.asList(UNIT, MTYPE);
    private static final TagCollection MINIMAL_TAGS;
    static {
        Map<String, String> kv = new HashMap<>();
        kv.put(MTYPE, "gauge");
        kv.put(UNIT, "metric");
        MINIMAL_TAGS = new TagCollection(kv);
    }

    private final String key;
    private final TagCollection tags;
    private final TagCollection meta;

    /**
     * Constructs a MetricDefinition with minimal tags and no meta tags
     */
    public MetricDefinition(String key) {
        this(key, MINIMAL_TAGS, TagCollection.EMPTY);
    }

    /**
     * Constructs a MetricDefinition with no meta tags or key
     */
    public MetricDefinition(TagCollection tags) {
        this(null, tags, TagCollection.EMPTY);
    }

    /**
     * Constructs a MetricDefinition with no key
     */
    public MetricDefinition(TagCollection tags, TagCollection meta) {
        this(null, tags, meta);
    }

    public MetricDefinition(String key, TagCollection tags, TagCollection meta) {
        if (tags == null) {
            throw new IllegalArgumentException("tags is required. Use TagCollection.EMPTY if you have no tags");
        }
        if (meta == null) {
            throw new IllegalArgumentException("meta is required. Use TagCollection.EMPTY if you have no meta tags");
        }
        for (String tag : REQUIRED_TAGS) {
            if (!tags.getKv().containsKey(tag)) {
                throw new IllegalArgumentException("Missing required tag: " + tag);
            }
        }
        this.key = key;
        this.tags = tags;
        this.meta = meta;
    }

    public String getKey() {
        return key;
    }

    public TagCollection getTags() {
        return tags;
    }

    public TagCollection getMeta() {
        return meta;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MetricDefinition that = (MetricDefinition) o;
        return Objects.equals(key, that.key) &&
                Objects.equals(tags, that.tags);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, tags);
    }

    @Override
    public String toString() {
        return "MetricDefinition{" +
                "key='" + key + '\'' +
                ", tags=" + tags +
                ", meta=" + meta +
                '}';
    }
}
