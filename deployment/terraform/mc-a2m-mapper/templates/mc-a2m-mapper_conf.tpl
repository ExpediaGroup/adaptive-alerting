anomaly-consumer {
  bootstrap.servers = "kafkasvc:9092"
  group.id = "mc-a2m-mapper"
  topic = "anomalies"
  key.deserializer = "org.apache.kafka.common.serialization.StringDeserializer"
  value.deserializer = "com.expedia.adaptivealerting.kafka.serde.AnomalyResultJsonDeserializer"
}
metric-producer {
  bootstrap.servers = "kafkasvc:9092"
  client.id = "mc-a2m-mapper"
  topic = "metrics"
  key.serializer = "org.apache.kafka.common.serialization.StringSerializer"
  value.serializer = "com.expedia.metrics.metrictank.MessagePackSerializer"
}
