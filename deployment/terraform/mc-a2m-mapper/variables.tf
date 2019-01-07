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
variable "kafka_input_endpoint" {}
variable "kafka_input_topic" {}
variable "kafka_input_serde_key" {}
variable "kafka_input_serde_value" {}
variable "kafka_input_extractor_timestamp" {}
variable "kafka_output_endpoint" {}
variable "kafka_output_topic" {}
variable "kafka_output_serde_key" {}
variable "kafka_output_serde_value" {}
