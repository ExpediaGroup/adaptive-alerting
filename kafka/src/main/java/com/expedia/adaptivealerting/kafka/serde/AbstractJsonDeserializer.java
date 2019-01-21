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
package com.expedia.adaptivealerting.kafka.serde;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;

import java.io.IOException;
import java.util.Map;

/**
 * Abstract base class for implementing JSON-based Kafka deserializers.
 *
 * @author Willie Wheeler
 * @param <T> Deserialization target class.
 */
public abstract class AbstractJsonDeserializer<T> implements Deserializer<T> {
    
    @Getter
    private Class<T> targetClass;
    
    @Getter
    private ObjectMapper objectMapper;
    
    public AbstractJsonDeserializer(Class<T> targetClass) {
        this.targetClass = targetClass;
        this.objectMapper = new ObjectMapper();
    }
    
    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
    }
    
    @Override
    public T deserialize(String topic, byte[] data) {
        if (data == null) {
            return null;
        }
        try {
            return objectMapper.readValue(data, targetClass);
        } catch (IOException e) {
            // According to the Kafka docs, this is for serialization rather than deserialization.
            // But there's no deserialization, and Spring Kafka does the same thing we're doing here. [WLW]
            throw new SerializationException(e);
        }
    }
    
    @Override
    public void close() {
    }
}
