package com.expedia.adaptivealerting.kafka.serde;

import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;

import java.util.Map;

public class JsonPojoSerde<T> implements Serde<T> {
    final private JsonPojoSerializer<T> serializer;
    final private JsonPojoDeserializer<T> deserializer;

    public JsonPojoSerde() {
        this.serializer = new JsonPojoSerializer<>();
        this.deserializer = new JsonPojoDeserializer<>();
    }

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
        serializer.configure(configs, isKey);
        deserializer.configure(configs, isKey);
    }

    @Override
    public void close() {
        serializer.close();
        deserializer.close();
    }

    @Override
    public Serializer<T> serializer() {
        return serializer;
    }

    @Override
    public Deserializer<T> deserializer() {
        return deserializer;
    }
}
