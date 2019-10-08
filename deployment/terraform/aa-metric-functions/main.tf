locals {
  app_name = "aa-metric-functions"
  config_file_path = "${path.module}/templates/aa-metric-functions_conf.tpl"
  deployment_yaml_file_path = "${path.module}/templates/deployment_yaml.tpl"
  count = "${var.enabled?1:0}"
  checksum = "${sha1("${data.template_file.config_data.rendered}")}"
  configmap1_name = "aa-metric-functions-config-${local.checksum}"
  configmap2_name = "functions.txt"

}

 data "template_file" "config_data" {
  template = "${file("${local.config_file_path}")}"
  vars {
        kafka_endpoint = "${var.kafka_endpoint}"
        aggregator_producer_topic = "${var.aggregator_producer_topic}"
        metric_source_graphite_host = "${var.metric_source_graphite_host}"
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
    configmap1_name = "${local.configmap1_name}"
    configmap2_name = "${local.configmap2_name}"

     # Environment
    jvm_memory_limit = "${var.jvm_memory_limit}"
    graphite_enabled = "${var.graphite_enabled}"
    graphite_host = "${var.graphite_hostname}"
    graphite_port = "${var.graphite_port}"
    graphite_prefix = "${var.graphite_prefix}"
    env_vars = "${indent(9,"${var.env_vars}")}"
  }
}

 resource "kubernetes_config_map" "aa-metric-functions-config" {
  metadata {
    name = "${local.configmap1_name}"
    namespace = "${var.namespace}"
  }
  data {
    "aa-metric-functions.conf" = "${data.template_file.config_data.rendered}"
  }
  count = "${local.count}"
}

 # Creating input file as config map via kubectl.
resource "null_resource" "kubectl_create_configmap" {
  provisioner "local-exec" {
    command = "${var.kubectl_executable_name} create configmap ${local.configmap2_name} --from-file=${var.metric_functions_input_file}"
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
