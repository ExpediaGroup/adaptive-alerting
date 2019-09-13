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
package com.expedia.adaptivealerting.kafka.alert;

import com.expedia.adaptivealerting.kafka.serde.AbstractJsonDeserializer;
import com.expedia.adaptivealerting.kafka.serde.AbstractJsonSerializer;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;

import java.util.Map;

/**
 * @deprecated Remove alerting from AA.
 */
public final class AlertJsonSerde implements Serde<Alert> {

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
    }

    @Override
    public void close() {
    }

    @Override
    public Serializer<Alert> serializer() {
        return new Ser();
    }

    @Override
    public Deserializer<Alert> deserializer() {
        return new Deser();
    }

    public static class Ser extends AbstractJsonSerializer<Alert> {
    }

    public static class Deser extends AbstractJsonDeserializer<Alert> {

        public Deser() {
            super(Alert.class);
        }
    }
}
