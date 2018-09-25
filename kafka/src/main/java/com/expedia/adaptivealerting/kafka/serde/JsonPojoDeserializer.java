/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 * Copyright 2018 The Apache Software Foundation.
 *
 * From: https://github.com/apache/kafka/blob/1.1.0/streams/examples/src/main/java/org/apache/kafka/streams/examples/pageview/JsonPOJODeserializer.java
 */
package com.expedia.adaptivealerting.kafka.serde;

import com.expedia.metrics.MetricData;
import com.expedia.metrics.MetricDefinition;
import com.expedia.metrics.TagCollection;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;

import java.util.Map;
import java.util.Set;

public class JsonPojoDeserializer<T> implements Deserializer<T> {
    private ObjectMapper objectMapper = new ObjectMapper();
    private Class<T> tClass;
    
    /**
     * Default constructor needed by Kafka
     */
    public JsonPojoDeserializer() {
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        
        // FIXME This stuff doesn't belong here, as it violates the Open/Closed Principle.
        // Chat with Peter about how we might most effectively incorporate the Jackson serde he created. [WLW]
        this.objectMapper.addMixIn(MetricData.class, MetricDataMixin.class);
        this.objectMapper.addMixIn(MetricDefinition.class, MetricDefinitionMixin.class);
        this.objectMapper.addMixIn(TagCollection.class, TagCollectionMixin.class);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public void configure(Map<String, ?> props, boolean isKey) {
        Object propClass = props.get("JsonPojoClass");
        if (propClass instanceof Class) {
            tClass = (Class<T>) propClass;
        } else {
            try {
                tClass = (Class<T>) Class.forName(propClass.toString());
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
    }
    
    @Override
    public T deserialize(String topic, byte[] bytes) {
        if (bytes == null)
            return null;
        
        T data;
        try {
            data = objectMapper.readValue(bytes, tClass);
        } catch (Exception e) {
            throw new SerializationException(e);
        }
        
        return data;
    }
    
    @Override
    public void close() {
    }
    
    private static class TagCollectionMixin {
        
        @JsonCreator
        TagCollectionMixin(@JsonProperty("kv") Map<String, String> kv, @JsonProperty("v") Set<String> v) {
        }
        
        @JsonIgnore
        boolean isEmpty() {
            return true;
        }
    }
    
    private static class MetricDefinitionMixin {
        
        @JsonCreator
        MetricDefinitionMixin(@JsonProperty("tags") TagCollection tags, @JsonProperty("meta") TagCollection meta) {
        }
    }
    
    private static class MetricDataMixin {
        
        @JsonCreator
        MetricDataMixin(
                @JsonProperty("metricDefinition") MetricDefinition metricDefinition,
                @JsonProperty("value") double value,
                @JsonProperty("timestamp") long timestamp) {
        }
    }
    
}
