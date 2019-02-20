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

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Deserializer;

import java.io.IOException;
import java.util.Map;

/**
 * Abstract base class for implementing JSON-based Kafka deserializers.
 *
 * @param <T> Deserialization target class.
 */
@Slf4j
public abstract class AbstractJsonDeserializer<T> implements Deserializer<T> {
    
    @Getter
    private Class<T> targetClass;
    
    @Getter
    private ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    
    public AbstractJsonDeserializer(Class<T> targetClass) {
        this.targetClass = targetClass;
    }
    
    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
        // Nothing to configure
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
//            throw new SerializationException(e);
            
            // Returning null per
            // https://stackoverflow.com/questions/51136942/how-to-handle-serializationexception-after-deserialization
            log.error("Deserialization error", e);
            return null;
        }
    }
    
    @Override
    public void close() {
        // Nothing to close
    }
}
