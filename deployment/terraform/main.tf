locals {
  kafka_endpoint =  "${var.kafka_hostname}:${var.kafka_port}"
}

# ========================================
# Adaptive Alerting
# ========================================

module "modelservice" {
  source = "modelservice"

  # Docker
  image = "expediadotcom/adaptive-alerting-modelservice:${var.alerting["version"]}"

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
  env_vars = "${var.modelservice["environment_overrides"]}"

  # App
  db_endpoint = "${var.modelservice["db_endpoint"]}"
}

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
  env_vars = "${var.ad-mapper["environment_overrides"]}"

  # App
  kafka_endpoint = "${local.kafka_endpoint}"
  modelservice_uri_template = "${var.ad-mapper["modelservice_uri_template"]}"
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
  env_vars = "${var.ad-manager["environment_overrides"]}"

  # App
  kafka_endpoint = "${local.kafka_endpoint}"
  modelservice_uri_template = "${var.ad-manager["modelservice_uri_template"]}"
}

module "mc-a2m-mapper" {
  source = "mc-a2m-mapper"

  # Docker
  image = "${var.mc-a2m-mapper["image"]}"
  image_pull_policy = "${var.mc-a2m-mapper["image_pull_policy"]}"

  # Kubernetes
  namespace = "${var.app_namespace}"
  enabled = "${var.mc-a2m-mapper["enabled"]}"
  replicas = "${var.mc-a2m-mapper["instances"]}"
  cpu_limit = "${var.mc-a2m-mapper["cpu_limit"]}"
  cpu_request = "${var.mc-a2m-mapper["cpu_request"]}"
  memory_limit = "${var.mc-a2m-mapper["memory_limit"]}"
  memory_request = "${var.mc-a2m-mapper["memory_request"]}"
  node_selector_label = "${var.node_selector_label}"
  kubectl_executable_name = "${var.kubectl_executable_name}"
  kubectl_context_name = "${var.kubectl_context_name}"

  # Environment
  graphite_enabled = "${var.graphite_enabled}"
  graphite_hostname = "${var.graphite_hostname}"
  graphite_port = "${var.graphite_port}"
  jvm_memory_limit = "${var.mc-a2m-mapper["jvm_memory_limit"]}"
  env_vars = "${var.mc-a2m-mapper["environment_overrides"]}"

  # App
  anomaly_consumer_bootstrap_servers = "${var.mc-a2m-mapper["anomaly_consumer_bootstrap_servers"]}"
  anomaly_consumer_group_id = "${var.mc-a2m-mapper["anomaly_consumer_group_id"]}"
  anomaly_consumer_topic = "${var.mc-a2m-mapper["anomaly_consumer_topic"]}"
  anomaly_consumer_key_deserializer = "${var.mc-a2m-mapper["anomaly_consumer_key_deserializer"]}"
  anomaly_consumer_value_deserializer = "${var.mc-a2m-mapper["anomaly_consumer_value_deserializer"]}"
  metric_producer_bootstrap_servers = "${var.mc-a2m-mapper["metric_producer_bootstrap_servers"]}"
  metric_producer_client_id = "${var.mc-a2m-mapper["metric_producer_client_id"]}"
  metric_producer_topic = "${var.mc-a2m-mapper["metric_producer_topic"]}"
  metric_producer_key_serializer = "${var.mc-a2m-mapper["metric_producer_key_serializer"]}"
  metric_producer_value_serializer = "${var.mc-a2m-mapper["metric_producer_value_serializer"]}"

}

module "notifier" {
  source = "notifier"

  # Docker
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
