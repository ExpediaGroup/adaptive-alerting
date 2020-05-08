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
  detector_mapping_filter_enabled = "${var.ad-mapper["detector_mapping_filter_enabled"]}"
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
  graphite_base_uri = "${var.ad-manager["graphite_base_uri"]}"
  graphite_data_retrieval_key = "${var.ad-manager["graphite_data_retrieval_key"]}"
  throttle_gate_likelihood = "${var.ad-manager["throttle_gate_likelihood"]}"
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

module "aa-metric-functions" {
  source = "aa-metric-functions"

  # Docker
  image = "${var.aa-metric-functions["image"]}"
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
  env_vars = "${var.aa-metric-functions["environment_overrides"]}"

  # App
  kafka_endpoint = "${local.kafka_endpoint}"
  metric_source_graphite_host = "${var.aa-metric-functions["metric_source_graphite_host"]}"
  aggregator_producer_topic = "${var.aa-metric-functions["aggregator_producer_topic"]}"
  metric_functions_input_file = "${var.aa-metric-functions["metric_functions_input_file"]}"
  is_graphite_server_metrictank = "${var.aa-metric-functions["is_graphite_server_metrictank"]}"
  initContainer_image = "${var.aa-metric-functions["initContainer_image"]}"
  download_input_file_command = "${var.aa-metric-functions["download_input_file_command"]}"
}

module "visualizer" {
  source = "visualizer"

  # Docker
  image = "${var.visualizer["image"]}"
  image_pull_policy = "${var.visualizer["image_pull_policy"]}"

  # Kubernetes
  namespace = "${var.app_namespace}"
  enabled = "${var.visualizer["enabled"]}"
  replicas = "${var.visualizer["instances"]}"
  cpu_limit = "${var.visualizer["cpu_limit"]}"
  cpu_request = "${var.visualizer["cpu_request"]}"
  memory_limit = "${var.visualizer["memory_limit"]}"
  memory_request = "${var.visualizer["memory_request"]}"
  node_selector_label = "${var.node_selector_label}"
  kubectl_executable_name = "${var.kubectl_executable_name}"
  kubectl_context_name = "${var.kubectl_context_name}"

  # Environment
  jvm_memory_limit = "${var.visualizer["jvm_memory_limit"]}"
  graphite_hostname = "${var.graphite_hostname}"
  graphite_port = "${var.graphite_port}"
  graphite_enabled = "${var.graphite_enabled}"
  graphite_prefix = "${var.graphite_prefix}"
  env_vars = "${var.visualizer["environment_overrides"]}"

  # App
  kafka_endpoint = "${local.kafka_endpoint}"
  group_id = "${var.visualizer["group_id"]}"
  topic = "${var.visualizer["topic"]}"
  key_deserializer = "${var.visualizer["key_deserializer"]}"
  value_deserializer = "${var.visualizer["value_deserializer"]}"
  elasticsearch_endpoint = "${var.visualizer["elasticsearch_endpoint"]}"
  elasticsearch_port1 = "${var.visualizer["elasticsearch_port1"]}"
  elasticsearch_port2 = "${var.visualizer["elasticsearch_port2"]}"
  elasticsearch_scheme = "${var.visualizer["elasticsearch_scheme"]}"
}
