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

  # Haystack tracing config
    tracing {
        tracingStatus = "${tracing_status}"
        apiKey = "${ad_mapper_tracing_apikey}"
        clientId = "${ad_mapper_tracing_clientid}"
        endpoint = "${haystack_collector_endpoint}"
        queueSize = "${haystack_tracer_queue_size}"
        flushInterval = "${haystack_tracer_flush_interval}"
        shutdownTimeout = "${haystack_tracer_shutdown_timeout}"
        threadCount = "${haystack_tracer_thread_count}"
    }
}
