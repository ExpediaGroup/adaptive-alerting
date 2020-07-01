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
variable "graphite_prefix" {
  default = ""
}
variable "env_vars" {}

# App
variable "kafka_endpoint" {}
variable "modelservice_base_uri" {}
variable "tracing_status" {}
variable "ad_mapper_tracing_apikey" {}
variable "ad_mapper_tracing_clientid" {}
variable "haystack_collector_endpoint" {}
variable "haystack_tracer_queue_size" {}
variable "haystack_tracer_flush_interval" {}
variable "haystack_tracer_shutdown_timeout" {}
variable "haystack_tracer_thread_count" {}
variable "detector_mapping_cache_update_period" {
  default = 5
}