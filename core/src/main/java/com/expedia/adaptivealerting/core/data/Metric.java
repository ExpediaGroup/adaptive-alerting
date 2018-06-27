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
package com.expedia.adaptivealerting.core.data;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static com.expedia.adaptivealerting.core.util.AssertUtil.notNull;

/**
 * Metrics 2.0 metric.
 *
 * @author Willie Wheeler
 */
public final class Metric {
    private final Map<String, String> tags = new HashMap<>();
    private final Map<String, String> meta = new HashMap<>();

    public Map<String, String> getTags() {
        return tags;
    }

    public Map<String, String> getMeta() {
        return meta;
    }

    public int tagsSize() {
        return tags.size();
    }

    public Set<String> tagNames() {
        return tags.keySet();
    }
    
    public String getTag(String name) {
        notNull(name, "name can't be null");
        return tags.get(name);
    }
    
    public void putTag(String name, String value) {
        notNull(name, "name can't be null");
        tags.put(name, value);
    }

    public void addTags(Map<String, String> tags) {
        notNull(tags, "tags can't be null");
        tags.entrySet().forEach(tag -> putTag(tag.getKey(), tag.getValue()));
    }

    public int metasSize() {
        return meta.size();
    }
    
    public Set<String> metaNames() {
        return meta.keySet();
    }
    
    public String getMeta(String name) {
        notNull(name, "name can't be null");
        return meta.get(name);
    }
    
    public void putMeta(String name, String value) {
        notNull(name, "name can't be null");
        meta.put(name, value);
    }
}
