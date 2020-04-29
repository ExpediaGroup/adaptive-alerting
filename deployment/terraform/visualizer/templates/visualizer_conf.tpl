visualizer {
    metric-consumer {
      bootstrap.servers = "localhost:19092"
      group.id = "my-group"
      topic = "anomalies"
      key.deserializer = "org.apache.kafka.common.serialization.StringDeserializer"
      value.deserializer = "com.expedia.adaptivealerting.kafka.serde.MappedMetricDataJsonSerde$Deser"
    }
    elastic-search {
      host = "localhost"
    }
}
