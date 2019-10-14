ad-mapper {
  streams {
    application.id = "ad-mapper-2"
    bootstrap.servers = "${kafka_endpoint}"
    default.value.serde = "com.expedia.adaptivealerting.kafka.serde.MetricDataMessagePackSerde"
    default.timestamp.extractor = "com.expedia.adaptivealerting.kafka.processor.MetricDataTimestampExtractor"
    auto.offset.reset = "latest"
    retries = 10
    retry.backoff.ms = 5000
  }
  health.status.path = "/app/isHealthy"
  inbound-topic = "aa-metrics"
  outbound-topic = "mapped-metrics"
  detector-mapping-cache-update-period = "${detector_mapping_cache_update_period}"
  model-service-base-uri = "${modelservice_base_uri}"
}
