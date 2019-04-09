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
variable "detector_mapper_es_urls" {}
variable "detector_mapper_es_config_vars_json" {}

# Unsure what this is, but we don't seem to be using it.
#variable "termination_grace_period" {
#  default = 30
#}
