locals {
  app_name = "metric-profiler"
  config_file_path = "${path.module}/templates/metric-profiler_conf.tpl"
  deployment_yaml_file_path = "${path.module}/templates/deployment_yaml.tpl"
  count = "${var.enabled?1:0}"
  checksum = "${sha1("${data.template_file.config_data.rendered}")}"
  configmap_name = "metric-profiler-${local.checksum}"
}

data "template_file" "config_data" {
  template = "${file("${local.config_file_path}")}"
  vars {
    kafka_endpoint = "${var.kafka_endpoint}"
    modelservice_base_uri = "${var.modelservice_base_uri}"
    inbound_topic = "${var.inbound_topic}"
  }
}

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
    graphite_prefix = "${var.graphite_prefix}"
    env_vars = "${indent(9,"${var.env_vars}")}"
  }
}

resource "kubernetes_config_map" "metric-profiler-config" {
  metadata {
    name = "${local.configmap_name}"
    namespace = "${var.namespace}"
  }
  data {
    "metric-profiler.conf" = "${data.template_file.config_data.rendered}"
  }
  count = "${local.count}"
}

# Deploying via kubectl since Terraform k8s provider doesn't natively support deployment.
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
