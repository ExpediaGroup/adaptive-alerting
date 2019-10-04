locals {
  kafka_endpoint =  "${var.kafka_hostname}:${var.kafka_port}"
}

# ========================================
# Adaptive Alerting
# ========================================

module "ad-mapper" {
  source = "ad-mapper"

  # Docker
  image = "${var.ad-mapper["image"]}"
  image_pull_policy = "${var.ad-mapper["image_pull_policy"]}"

  # Kubernetes
  namespace = "${var.app_namespace}"
  enabled = "${var.ad-mapper["enabled"]}"
  replicas = "${var.ad-mapper["instances"]}"
  cpu_limit = "${var.ad-mapper["cpu_limit"]}"
  cpu_request = "${var.ad-mapper["cpu_request"]}"
  memory_limit = "${var.ad-mapper["memory_limit"]}"
  memory_request = "${var.ad-mapper["memory_request"]}"
  node_selector_label = "${var.node_selector_label}"
  kubectl_executable_name = "${var.kubectl_executable_name}"
  kubectl_context_name = "${var.kubectl_context_name}"

  # Environment
  jvm_memory_limit = "${var.ad-mapper["jvm_memory_limit"]}"
  graphite_hostname = "${var.graphite_hostname}"
  graphite_port = "${var.graphite_port}"
  graphite_enabled = "${var.graphite_enabled}"
  graphite_prefix = "${var.graphite_prefix}"
  env_vars = "${var.ad-mapper["environment_overrides"]}"

  # App
  kafka_endpoint = "${local.kafka_endpoint}"
  modelservice_base_uri = "${var.ad-mapper["modelservice_base_uri"]}"
  detector_mapping_cache_update_period = "${var.ad-mapper["detector_mapping_cache_update_period"]}"
}

module "ad-manager" {
  source = "ad-manager"

  # Docker
  image = "${var.ad-manager["image"]}"
  image_pull_policy = "${var.ad-manager["image_pull_policy"]}"

  # Kubernetes
  namespace = "${var.app_namespace}"
  enabled = "${var.ad-manager["enabled"]}"
  replicas = "${var.ad-manager["instances"]}"
  cpu_limit = "${var.ad-manager["cpu_limit"]}"
  cpu_request = "${var.ad-manager["cpu_request"]}"
  memory_limit = "${var.ad-manager["memory_limit"]}"
  memory_request = "${var.ad-manager["memory_request"]}"
  node_selector_label = "${var.node_selector_label}"
  kubectl_executable_name = "${var.kubectl_executable_name}"
  kubectl_context_name = "${var.kubectl_context_name}"

  # Environment
  jvm_memory_limit = "${var.ad-manager["jvm_memory_limit"]}"
  graphite_enabled = "${var.graphite_enabled}"
  graphite_hostname = "${var.graphite_hostname}"
  graphite_port = "${var.graphite_port}"
  graphite_prefix = "${var.graphite_prefix}"
  env_vars = "${var.ad-manager["environment_overrides"]}"

  # App
  metric_consumer_bootstrap_servers = "${var.ad-manager["metric_consumer_bootstrap_servers"]}"
  metric_consumer_group_id = "${var.ad-manager["metric_consumer_group_id"]}"
  metric_consumer_topic = "${var.ad-manager["metric_consumer_topic"]}"
  metric_consumer_key_deserializer = "${var.ad-manager["metric_consumer_key_deserializer"]}"
  metric_consumer_value_deserializer = "${var.ad-manager["metric_consumer_value_deserializer"]}"
  anomaly_producer_bootstrap_servers = "${var.ad-manager["anomaly_producer_bootstrap_servers"]}"
  anomaly_producer_client_id = "${var.ad-manager["anomaly_producer_client_id"]}"
  anomaly_producer_outlier_topic = "${var.ad-manager["anomaly_producer_outlier_topic"]}"
  anomaly_producer_breakout_topic = "${var.ad-manager["anomaly_producer_breakout_topic"]}"
  anomaly_producer_key_serializer = "${var.ad-manager["anomaly_producer_key_serializer"]}"
  anomaly_producer_value_serializer = "${var.ad-manager["anomaly_producer_value_serializer"]}"
  modelservice_base_uri = "${var.ad-manager["modelservice_base_uri"]}"
  detector_refresh_period = "${var.ad-manager["detector_refresh_period"]}"
}

module "modelservice" {
  source = "modelservice"

  # Docker
  image = "${var.modelservice["image"]}"
  image_pull_policy = "${var.modelservice["image_pull_policy"]}"

  # Kubernetes
  namespace = "${var.app_namespace}"
  enabled = "${var.modelservice["enabled"]}"
  replicas = "${var.modelservice["instances"]}"
  cpu_limit = "${var.modelservice["cpu_limit"]}"
  cpu_request = "${var.modelservice["cpu_request"]}"
  memory_limit = "${var.modelservice["memory_limit"]}"
  memory_request = "${var.modelservice["memory_request"]}"
  node_selector_label = "${var.node_selector_label}"
  kubectl_executable_name = "${var.kubectl_executable_name}"
  kubectl_context_name = "${var.kubectl_context_name}"

  # Environment
  jvm_memory_limit = "${var.modelservice["jvm_memory_limit"]}"
  graphite_hostname = "${var.graphite_hostname}"
  graphite_port = "${var.graphite_port}"
  graphite_enabled = "${var.graphite_enabled}"
  graphite_prefix = "${var.graphite_prefix}"
  env_vars = "${var.modelservice["environment_overrides"]}"

  # App
  db_endpoint = "${var.modelservice["db_endpoint"]}"
  graphite_url = "${var.modelservice["graphite_url"]}"
  detector_mapper_es_urls = "${var.modelservice["detector_mapper_es_urls"]}"
  detector_mapper_index_name = "${var.modelservice["detector_mapper_index_name"]}"
  detector_mapper_doctype = "${var.modelservice["detector_mapper_doctype"]}"
  detector_index_name = "${var.modelservice["detector_index_name"]}"
  detector_doctype = "${var.modelservice["detector_doctype"]}"
  detector_mapper_es_config_connection_timeout = "${var.modelservice["detector_mapper_es_config_connection_timeout"]}"
  detector_mapper_es_config_connection_retry_timeout = "${var.modelservice["detector_mapper_es_config_connection_retry_timeout"]}"
  detector_mapper_es_config_max_total_connection = "${var.modelservice["detector_mapper_es_config_max_total_connection"]}"
  # Additional security for es in AWS
  detector_mapper_es_config_aws_iam_auth_required = "${var.modelservice["detector_mapper_es_config_aws_iam_auth_required"]}"
}

module "a2m-mapper" {
  source = "a2m-mapper"

  # Docker
  image = "${var.a2m-mapper["image"]}"
  image_pull_policy = "${var.a2m-mapper["image_pull_policy"]}"

  # Kubernetes
  namespace = "${var.app_namespace}"
  enabled = "${var.a2m-mapper["enabled"]}"
  replicas = "${var.a2m-mapper["instances"]}"
  cpu_limit = "${var.a2m-mapper["cpu_limit"]}"
  cpu_request = "${var.a2m-mapper["cpu_request"]}"
  memory_limit = "${var.a2m-mapper["memory_limit"]}"
  memory_request = "${var.a2m-mapper["memory_request"]}"
  node_selector_label = "${var.node_selector_label}"
  kubectl_executable_name = "${var.kubectl_executable_name}"
  kubectl_context_name = "${var.kubectl_context_name}"

  # Environment
  graphite_enabled = "${var.graphite_enabled}"
  graphite_hostname = "${var.graphite_hostname}"
  graphite_port = "${var.graphite_port}"
  jvm_memory_limit = "${var.a2m-mapper["jvm_memory_limit"]}"
  env_vars = "${var.a2m-mapper["environment_overrides"]}"

  # App
  anomaly_consumer_bootstrap_servers = "${var.a2m-mapper["anomaly_consumer_bootstrap_servers"]}"
  anomaly_consumer_group_id = "${var.a2m-mapper["anomaly_consumer_group_id"]}"
  anomaly_consumer_topic = "${var.a2m-mapper["anomaly_consumer_topic"]}"
  anomaly_consumer_key_deserializer = "${var.a2m-mapper["anomaly_consumer_key_deserializer"]}"
  anomaly_consumer_value_deserializer = "${var.a2m-mapper["anomaly_consumer_value_deserializer"]}"
  metric_producer_bootstrap_servers = "${var.a2m-mapper["metric_producer_bootstrap_servers"]}"
  metric_producer_client_id = "${var.a2m-mapper["metric_producer_client_id"]}"
  metric_producer_topic = "${var.a2m-mapper["metric_producer_topic"]}"
  metric_producer_key_serializer = "${var.a2m-mapper["metric_producer_key_serializer"]}"
  metric_producer_value_serializer = "${var.a2m-mapper["metric_producer_value_serializer"]}"
}

module "metric-profiler" {
  source = "metric-profiler"

  # Docker
  image = "${var.metric-profiler["image"]}"
  image_pull_policy = "${var.metric-profiler["image_pull_policy"]}"

  # Kubernetes
  namespace = "${var.app_namespace}"
  enabled = "${var.metric-profiler["enabled"]}"
  replicas = "${var.metric-profiler["instances"]}"
  cpu_limit = "${var.metric-profiler["cpu_limit"]}"
  cpu_request = "${var.metric-profiler["cpu_request"]}"
  memory_limit = "${var.metric-profiler["memory_limit"]}"
  memory_request = "${var.metric-profiler["memory_request"]}"
  node_selector_label = "${var.node_selector_label}"
  kubectl_executable_name = "${var.kubectl_executable_name}"
  kubectl_context_name = "${var.kubectl_context_name}"

  # Environment
  jvm_memory_limit = "${var.metric-profiler["jvm_memory_limit"]}"
  graphite_hostname = "${var.graphite_hostname}"
  graphite_port = "${var.graphite_port}"
  graphite_enabled = "${var.graphite_enabled}"
  graphite_prefix = "${var.graphite_prefix}"
  env_vars = "${var.metric-profiler["environment_overrides"]}"

  # App
  kafka_endpoint = "${local.kafka_endpoint}"
  modelservice_base_uri = "${var.metric-profiler["modelservice_base_uri"]}"
  inbound_topic = "${var.metric-profiler["inbound_topic"]}"
}

module "aa-metric-functions" {
  source = "aa-metric-functions"

  # Docker
  image = "${var.metric-functions["image"]}"
  image_pull_policy = "${var.aa-metric-functions["image_pull_policy"]}"

  # Kubernetes
  namespace = "${var.app_namespace}"
  enabled = "${var.aa-metric-functions["enabled"]}"
  replicas = "${var.aa-metric-functions["instances"]}"
  cpu_limit = "${var.aa-metric-functions["cpu_limit"]}"
  cpu_request = "${var.aa-metric-functions["cpu_request"]}"
  memory_limit = "${var.aa-metric-functions["memory_limit"]}"
  memory_request = "${var.aa-metric-functions["memory_request"]}"
  node_selector_label = "${var.node_selector_label}"
  kubectl_executable_name = "${var.kubectl_executable_name}"
  kubectl_context_name = "${var.kubectl_context_name}"

  # Environment
  jvm_memory_limit = "${var.aa-metric-functions["jvm_memory_limit"]}"
  graphite_hostname = "${var.graphite_hostname}"
  graphite_port = "${var.graphite_port}"
  graphite_enabled = "${var.graphite_enabled}"
  graphite_prefix = "${var.graphite_prefix}"
  env_vars = "${var.aa-metric-functions["environment_overrides"]}"

  # App
  kafka_endpoint = "${local.kafka_endpoint}"
  modelservice_base_uri = "${var.aa-metric-functions["modelservice_base_uri"]}"
  inbound_topic = "${var.aa-metric-functions["inbound_topic"]}"
}

module "notifier" {
  source = "notifier"

  # Docker
  # TODO Update the image to source from var.notifier["image"]
  image = "expediadotcom/adaptive-alerting-notifier:${var.alerting["version"]}"
  image_pull_policy = "${var.notifier["image_pull_policy"]}"

  # Kubernetes
  namespace = "${var.app_namespace}"
  enabled = "${var.notifier["enabled"]}"
  replicas = "${var.notifier["instances"]}"
  cpu_limit = "${var.notifier["cpu_limit"]}"
  cpu_request = "${var.notifier["cpu_request"]}"
  memory_limit = "${var.notifier["memory_limit"]}"
  memory_request = "${var.notifier["memory_request"]}"
  node_selector_label = "${var.node_selector_label}"
  kubectl_executable_name = "${var.kubectl_executable_name}"
  kubectl_context_name = "${var.kubectl_context_name}"

  # Environment
  jvm_memory_limit = "${var.notifier["jvm_memory_limit"]}"
  graphite_hostname = "${var.graphite_hostname}"
  graphite_port = "${var.graphite_port}"
  graphite_enabled = "${var.graphite_enabled}"
  env_vars = "${var.notifier["environment_overrides"]}"

  # App
  kafka_endpoint = "${local.kafka_endpoint}"
  webhook_url = "${var.notifier["webhook_url"]}"
}
