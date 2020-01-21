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
  graphite-base-uri = "${graphite-base-uri}"
  graphite-earliest-time ="${graphite-earliest-time}"
  graphite-max-data-points = "${graphite-max-data-point}"
  graphite-data-retrieval-key = "${graphite-data-retrieval-key}"

  # Detector refresh period in minutes
  detector-refresh-period = ${detector_refresh_period}
}
