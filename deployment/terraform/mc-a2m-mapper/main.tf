locals {
  app_name = "mc-a2m-mapper"
  config_file_path = "${path.module}/templates/mc-a2m-mapper_conf.tpl"
  deployment_yaml_file_path = "${path.module}/templates/deployment_yaml.tpl"
  count = "${var.enabled?1:0}"
  checksum = "${sha1("${data.template_file.config_data.rendered}")}"
  configmap_name = "mc-a2m-mapper-${local.checksum}"
}

data "template_file" "config_data" {
  template = "${file("${local.config_file_path}")}"
  vars {
    kafka_input_endpoint = "${var.kafka_input_endpoint}"
    kafka_input_topic = "${var.kafka_input_topic}"
    kafka_input_serde_key = "${var.kafka_input_serde_key}"
    kafka_input_serde_value = "${var.kafka_input_serde_value}"
    kafka_input_extractor_timestamp = "${var.kafka_input_extractor_timestamp}"
    kafka_output_endpoint = "${var.kafka_output_endpoint}"
    kafka_output_topic = "${var.kafka_output_topic}"
    kafka_output_serde_key = "${var.kafka_output_serde_key}"
    kafka_output_serde_value = "${var.kafka_output_serde_value}"
  }
}

// using kubectl to create deployment construct since its not natively support by the kubernetes provider
data "template_file" "deployment_yaml" {
  template = "${file("${local.deployment_yaml_file_path}")}"
  vars {
    app_name = "${local.app_name}"

    # Docker
    image = "${var.image}"
    image_pull_policy = "${var.image_pull_policy}"

    # Kubernetes
    namespace = "${var.namespace}"
    replicas = "${var.replicas}"
    cpu_limit = "${var.cpu_limit}"
    cpu_request = "${var.cpu_request}"
    memory_limit = "${var.memory_limit}"
    memory_request = "${var.memory_request}"
    node_selector_label = "${var.node_selector_label}"
    configmap_name = "${local.configmap_name}"

    # Environment
    jvm_memory_limit = "${var.jvm_memory_limit}"
    graphite_enabled = "${var.graphite_enabled}"
    graphite_host = "${var.graphite_hostname}"
    graphite_port = "${var.graphite_port}"
    env_vars = "${indent(9,"${var.env_vars}")}"
  }
}

resource "kubernetes_config_map" "mc-a2m-mapper-config" {
  metadata {
    name = "${local.configmap_name}"
    namespace = "${var.namespace}"
  }
  data {
    "mc-a2m-mapper.conf" = "${data.template_file.config_data.rendered}"
  }
  count = "${local.count}"
}

resource "null_resource" "kubectl_apply" {
  triggers {
    template = "${data.template_file.deployment_yaml.rendered}"
  }
  provisioner "local-exec" {
    command = "echo '${data.template_file.deployment_yaml.rendered}' | ${var.kubectl_executable_name} apply -f - --context ${var.kubectl_context_name}"
  }
  count = "${local.count}"
}

resource "null_resource" "kubectl_destroy" {
  provisioner "local-exec" {
    command = "echo '${data.template_file.deployment_yaml.rendered}' | ${var.kubectl_executable_name} delete -f - --context ${var.kubectl_context_name}"
    when = "destroy"
  }
  count = "${local.count}"
}
