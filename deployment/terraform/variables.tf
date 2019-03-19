variable "app_namespace" {}
variable "kafka_hostname" {}
variable "kafka_port" {}
variable "graphite_hostname" {}
variable "graphite_port" {}
variable "graphite_enabled" {}
variable "graphite_prefix" {
  default = ""
}
variable "node_selector_label"{}
variable "kubectl_context_name" {}
variable "kubectl_executable_name" {}

variable "alerting" {
  type = "map"
}

variable "ad-mapper" {
  type = "map"
}

variable "ad-manager" {
  type = "map"
}

variable "modelservice" {
  type = "map"
}

variable "a2m-mapper" {
  type = "map"
}

variable "notifier" {
  type = "map"
}
