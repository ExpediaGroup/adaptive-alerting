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

import com.expedia.metrics.MetricData;
import com.expedia.metrics.MetricDefinition;
import com.expedia.metrics.TagCollection;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.module.SimpleModule;

import java.util.Map;
import java.util.Set;

// TODO Replace with metrics-java version once it's released. [WLW]

/**
 * Jackson module for Metrics Java.
 *
 * @author Willie Wheeler
 */
public final class MetricsJavaModule extends SimpleModule {
    
    public MetricsJavaModule() {
        setMixInAnnotation(MetricData.class, MetricDataMixin.class);
        setMixInAnnotation(MetricDefinition.class, MetricDefinitionMixin.class);
        setMixInAnnotation(TagCollection.class, TagCollectionMixin.class);
    }
    
    private static class MetricDataMixin {
        
        @JsonCreator
        public MetricDataMixin(
                @JsonProperty("metricDefinition") MetricDefinition metricDefinition,
                @JsonProperty("value") double value,
                @JsonProperty("timestamp") long timestamp) {
        }
    }
    
    private static class MetricDefinitionMixin {
        
        @JsonCreator
        public MetricDefinitionMixin(
                @JsonProperty("tags") TagCollection tags,
                @JsonProperty("meta") TagCollection meta) {
        }
    }
    
    private static class TagCollectionMixin {
        
        @JsonCreator
        public TagCollectionMixin(
                @JsonProperty("kv") Map<String, String> kv,
                @JsonProperty("v") Set<String> v) {
        }
        
        @JsonIgnore
        boolean isEmpty() {
            return true;
        }
    }
}
