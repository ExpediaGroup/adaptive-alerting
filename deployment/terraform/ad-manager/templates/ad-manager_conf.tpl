ad-manager {
  streams {
    application.id = "ad-manager"
    bootstrap.servers = "${kafka_endpoint}"
    default.value.serde = "com.expedia.adaptivealerting.kafka.serde.MappedMetricDataJsonSerde"
    default.timestamp.extractor = "com.expedia.adaptivealerting.kafka.processor.MappedMetricDataTimestampExtractor"
  }
  health.status.path = "/app/isHealthy"
  inbound-topic = "mapped-metrics"
  outbound-topic = "anomalies"
  model-service-uri-template = "${modelservice_uri_template}"
}
