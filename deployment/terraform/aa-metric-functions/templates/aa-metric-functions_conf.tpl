aa-metric-functions {
  aggregator-producer {
    bootstrap.servers = "${kafka_endpoint}"
    client.id = "aa-aggregator-producer"
    key.serializer = "org.apache.kafka.common.serialization.StringSerializer"
    value.serializer = "com.expedia.adaptivealerting.kafka.serde.MetricDataMessagePackSerde$Ser"
  }
  metric-source-sink {
    metric-source = "graphite"
    urlTemplate: "${metric_source_graphite_host}/render?format=json&target="
    graphite-host = "${metric_source_graphite_host}"
    output_topic = "${aggregator_producer_topic}"
    is-graphite-server-metrictank = "${is_graphite_server_metrictank}"
  }
} 
