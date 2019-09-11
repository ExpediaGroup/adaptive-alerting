metric-profiler {
  streams {
    application.id = "metric-profiler"
    bootstrap.servers = "${kafka_endpoint}"
    default.value.serde = "com.expedia.adaptivealerting.kafka.serde.MetricDataMessagePackSerde"
    default.timestamp.extractor = "com.expedia.adaptivealerting.kafka.processor.MetricDataTimestampExtractor"
    retries = 10
    retry.backoff.ms = 5000
  }
  health.status.path = "/app/isHealthy"
  inbound-topic = "${inbound_topic}"
  outbound-topic = "profile-metrics"
  model-service-base-uri = "${modelservice_base_uri}"
}
