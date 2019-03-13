ad-mapper {
  streams {
    application.id = "ad-mapper"
    bootstrap.servers = "${kafka_endpoint}"
    default.value.serde = "com.expedia.adaptivealerting.kafka.serde.MetricDataMessagePackSerde"
    default.timestamp.extractor = "com.expedia.adaptivealerting.kafka.processor.MetricDataTimestampExtractor"
  }
  health.status.path = "/app/isHealthy"
  inbound-topic = "aa-metrics"
  outbound-topic = "mapped-metrics"
  model-service-uri-template = "${modelservice_uri_template}"
}
