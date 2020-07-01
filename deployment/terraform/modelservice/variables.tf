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
variable "service_port" {
  default = 80
}
variable "container_port" {
  default = 8080
}
variable "node_selector_label" {}
variable "kubectl_executable_name" {}
variable "kubectl_context_name" {}

# Environment
variable "jvm_memory_limit" {}
variable "graphite_hostname" {}
variable "graphite_port" {}
variable "graphite_enabled" {}
variable "graphite_prefix" {}
variable "env_vars" {}

# App
variable "db_endpoint" {}
variable "graphite_url" {}
variable "detector_mapper_es_urls" {}
variable "detector_mapper_index_name" {}
variable "detector_mapper_doctype" {}
variable "detector_index_name" {}
variable "detector_doctype" {}
variable "detector_mapper_es_config_connection_timeout" {}
variable "detector_mapper_es_config_connection_retry_timeout" {}
variable "detector_mapper_es_config_max_total_connection" {}
variable "detector_mapper_es_config_aws_iam_auth_required" {}
variable "modelservice_haystack_tracer_apikey" {}
variable "modelservice_haystack_tracer_clientid" {}
variable "haystack_collector_endpoint" {}
variable "haystack_tracer_queue_size" {}
variable "haystack_tracer_flush_interval" {}
variable "haystack_tracer_shutdown_timeout" {}
variable "haystack_tracer_thread_count" {}

# Unsure what this is, but we don't seem to be using it.
#variable "termination_grace_period" {
#  default = 30
#}
