ad-manager {
  streams {
    application.id = "ad-manager"
    bootstrap.servers = "${kafka_endpoint}"
    default.value.serde = "com.expedia.adaptivealerting.kafka.serde.MappedMetricDataJsonSerde"
    default.timestamp.extractor = "com.expedia.adaptivealerting.kafka.processor.MappedMetricDataTimestampExtractor"
    retries = 10
    retry.backoff.ms = 5000
  }
  health.status.path = "/app/isHealthy"
  inbound-topic = "mapped-metrics"
  outbound-topic = "anomalies"
  detector-refresh-period = "${detector_refresh_period}"
  model-service-base-uri = "${modelservice_base_uri}"
}
