visualizer {
    metric-consumer {
      bootstrap.servers = "${kafka_endpoint}"
      group.id = "${group_id}"
      topic = "${topic}"
      key.deserializer = "${key_deserializer}"
      value.deserializer = "${value_deserializer}"
    }
    elastic-search {
      host = "${elasticsearch_endpoint}"
    }
}
