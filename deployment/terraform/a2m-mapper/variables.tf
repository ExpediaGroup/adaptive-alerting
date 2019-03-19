# Docker
variable "image" {}
variable "image_pull_policy" {
  default = "IfNotPresent"
}

# Kubernetes
variable "namespace" {}
variable "enabled" {}
variable "replicas" {}
variable "cpu_limit" {}
variable "cpu_request" {}
variable "memory_limit" {}
variable "memory_request" {}
variable "node_selector_label" {}
variable "kubectl_executable_name" {}
variable "kubectl_context_name" {}

# Environment
variable "jvm_memory_limit" {}
variable "graphite_hostname" {}
variable "graphite_port" {}
variable "graphite_enabled" {}
variable "env_vars" {}

# App
variable "anomaly_consumer_bootstrap_servers" {}
variable "anomaly_consumer_group_id" {}
variable "anomaly_consumer_topic" {}
variable "anomaly_consumer_key_deserializer" {}
variable "anomaly_consumer_value_deserializer" {}
variable "metric_producer_bootstrap_servers" {}
variable "metric_producer_client_id" {}
variable "metric_producer_topic" {}
variable "metric_producer_key_serializer" {}
variable "metric_producer_value_serializer" {}
