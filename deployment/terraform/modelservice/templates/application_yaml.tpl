spring:
  main:
    web-environment: true
    show-banner: true
endpoints:
  health:
    sensitive: false
management:
  context-path: "/admin"
  metrics:
    export:
      jmx:
        domain: spring
    enable:
      jvm: false
      tomcat: false
      system: false
      process: false

# Elasticsearch
datasource-es:
  createIndexIfNotFound: true
  indexName: ${detector_mapper_index_name}
  doctype: ${detector_mapper_doctype}
  detectorIndexName: ${detector_index_name}
  detectorDocType: ${detector_doctype}
  urls: ${detector_mapper_es_urls}
  config: 
    connectionTimeout: ${detector_mapper_es_config_connection_timeout}
    connectionRetryTimeout: ${detector_mapper_es_config_connection_retry_timeout}
    maxTotalConnection:  ${detector_mapper_es_config_max_total_connection}
    awsIamAuthRequired: ${detector_mapper_es_config_aws_iam_auth_required}

# Graphite data source
graphite:
  urlTemplate: "${graphite_url}"

# Swagger Documentation
swagger:
  service:
    version: "1.0.0"
    title: "Model service detector mapper"
    description: "API documentation for model service detector mappings"

# AWS
security:
  signingKey: $${SIGNING_KEY}
