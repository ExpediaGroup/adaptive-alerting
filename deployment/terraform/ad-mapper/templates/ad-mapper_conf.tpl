ad-mapper {
  streams {
    application.id = "ad-mapper"
    bootstrap.servers = "${kafka_endpoint}"
    default.value.serde = "com.expedia.adaptivealerting.kafka.serde.MetricDataMessagePackSerde"
    default.timestamp.extractor = "com.expedia.adaptivealerting.kafka.processor.MetricDataTimestampExtractor"
    retries = 10
    retry.backoff.ms = 5000
  }
  health.status.path = "/app/isHealthy"
  inbound-topic = "aa-metrics"
  outbound-topic = "mapped-metrics"
  detector-mapping-cache-update-period = "${detector_mapping_cache_update_period}"
  model-service-base-uri = "${modelservice_base_uri}"
  detector-mapping-filter-enabled = "${detector_mapping_filter_enabled}"
}
