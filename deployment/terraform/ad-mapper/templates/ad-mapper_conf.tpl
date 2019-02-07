ad-mapper {
  streams {
    application.id = "ad-mapper"
    bootstrap.servers = "${kafka_endpoint}"
    default.value.serde = "com.expedia.adaptivealerting.kafka.serde.MetricDataSerde"
    default.timestamp.extractor = "com.expedia.adaptivealerting.kafka.serde.MetricDataTimestampExtractor"
  }
  health.status.path = "/app/isHealthy"
  inbound-topic = "aa-metrics"
  outbound-topic = "mapped-metrics"
  detector-class-map {
    constant-detector = "com.expedia.adaptivealerting.anomdetect.constant.ConstantThresholdAnomalyDetector"
    cusum-detector = "com.expedia.adaptivealerting.anomdetect.cusum.CusumAnomalyDetector"
    ewma-detector = "com.expedia.adaptivealerting.anomdetect.ewma.EwmaAnomalyDetector"
    pewma-detector = "com.expedia.adaptivealerting.anomdetect.pewma.PewmaAnomalyDetector"
    holtwinters-detector = "com.expedia.adaptivealerting.anomdetect.holtwinters.HoltWintersAnomalyDetector"
  }
  model-service-uri-template = "${modelservice_uri_template}"
}
