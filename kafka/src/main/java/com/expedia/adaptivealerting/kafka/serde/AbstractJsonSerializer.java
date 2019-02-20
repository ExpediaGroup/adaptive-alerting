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
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Serializer;

import java.util.Map;

/**
 * Abstract base class for implementing JSON-based Kafka serializers.
 *
 * @author Willie Wheeler
 * @param <T> Serialization target class.
 */
@Slf4j
public class AbstractJsonSerializer<T> implements Serializer<T> {
    
    @Getter
    private ObjectMapper objectMapper = new ObjectMapper();
    
    @Override
    public void configure(Map<String, ?> map, boolean b) {
        // Nothing to configure
    }
    
    @Override
    public byte[] serialize(String topic, T data) {
        if (data == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsBytes(data);
        } catch (Exception e) {
            throw new SerializationException("Error serializing data to JSON", e);
        }
    }
    
    @Override
    public void close() {
        // Nothing to close
    }
}
