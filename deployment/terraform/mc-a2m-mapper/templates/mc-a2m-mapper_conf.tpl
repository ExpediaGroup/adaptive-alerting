mc-a2m-mapper {
  anomaly-consumer {
    bootstrap.servers = "${anomaly_consumer_bootstrap_servers}"
    group.id = "${anomaly_consumer_group_id}"
    topic = "${anomaly_consumer_topic}"
    key.deserializer = "${anomaly_consumer_key_deserializer}"
    value.deserializer = "${anomaly_consumer_value_deserializer}"
  }
  metric-producer {
    bootstrap.servers = "${metric_producer_bootstrap_servers}"
    client.id = "${metric_producer_client_id}"
    topic = "${metric_producer_topic}"
    key.serializer = "${metric_producer_key_serializer}"
    value.serializer = "${metric_producer_value_serializer}"
  }
}
