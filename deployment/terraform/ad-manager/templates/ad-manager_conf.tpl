ad-manager {
  streams {
    application.id = "ad-manager"
    bootstrap.servers = "${kafka_endpoint}"
    default.value.serde = "com.expedia.adaptivealerting.kafka.serde.JsonPojoSerde"
    JsonPojoClass = "com.expedia.adaptivealerting.core.data.MappedMetricData"
    default.timestamp.extractor = "com.expedia.adaptivealerting.kafka.serde.MappedMetricDataTimestampExtractor"
  }
  health.status.path = "/app/isHealthy"
  inbound-topic = "mapped-metrics"
  outbound-topic = "anomalies"
  detector-class-map {
    constant-detector = "com.expedia.adaptivealerting.anomdetect.constant.ConstantThresholdAnomalyDetector"
    cusum-detector = "com.expedia.adaptivealerting.anomdetect.cusum.CusumAnomalyDetector"
    ewma-detector = "com.expedia.adaptivealerting.anomdetect.ewma.EwmaAnomalyDetector"
    pewma-detector = "com.expedia.adaptivealerting.anomdetect.pewma.PewmaAnomalyDetector"
  }
  model-service-uri-template = "${modelservice_uri_template}"
}
