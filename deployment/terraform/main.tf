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
  detector_refresh_period = "${var.ad-mapper["detector_refresh_period"]}"
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
  kafka_endpoint = "${local.kafka_endpoint}"
  modelservice_base_uri = "${var.ad-mapper["modelservice_base_uri"]}"
  detector_refresh_period = "${var.ad-mapper["detector_refresh_period"]}"
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
  detector_mapper_es_urls = "${var.modelservice["detector_mapper_es_urls"]}"
  detector_mapper_es_config_vars_json = "${var.modelservice["detector_mapper_es_config_vars_json"]}"
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
