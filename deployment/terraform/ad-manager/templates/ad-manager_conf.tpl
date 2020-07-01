ad-manager {
  metric-consumer {
    bootstrap.servers = "${metric_consumer_bootstrap_servers}"
    group.id = "${metric_consumer_group_id}"
    topic = "${metric_consumer_topic}"
    key.deserializer = "${metric_consumer_key_deserializer}"
    value.deserializer = "${metric_consumer_value_deserializer}"
  }
  anomaly-producer {
    bootstrap.servers = "${anomaly_producer_bootstrap_servers}"
    client.id = "${anomaly_producer_client_id}"
    outlier-topic = "${anomaly_producer_outlier_topic}"
    breakout-topic = "${anomaly_producer_breakout_topic}"
    key.serializer = "${anomaly_producer_key_serializer}"
    value.serializer = "${anomaly_producer_value_serializer}"
  }
  model-service-base-uri = "${modelservice_base_uri}"

 # Graphite data source config
  graphite-base-uri = "${graphite_base_uri}"
  graphite-data-retrieval-key = "${graphite_data_retrieval_key}"
  throttle-gate-likelihood = "${throttle_gate_likelihood}"

  # Detector refresh period in minutes
  detector-refresh-period = ${detector_refresh_period}

  # Haystack tracing config
  tracing {
      tracingStatus = "${ad_manager_tracing_status}"
      apiKey = "${ad_manager_tracing_apikey}"
      clientId = "${ad_manager_tracing_clientid}"
      endpoint = "${haystack_collector_endpoint}"
      queueSize = "${haystack_tracer_queue_size}"
      flushInterval = "${haystack_tracer_flush_interval}"
      shutdownTimeout = "${haystack_tracer_shutdown_timeout}"
      threadCount = "${haystack_tracer_thread_count}"
  }
}
