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
  detector-refresh-period = "${detector_refresh_period}"
  model-service-base-uri = "${modelservice_base_uri}"
}
