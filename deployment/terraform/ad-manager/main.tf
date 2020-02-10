locals {
  app_name = "ad-manager"
  config_file_path = "${path.module}/templates/ad-manager_conf.tpl"
  deployment_yaml_file_path = "${path.module}/templates/deployment_yaml.tpl"
  count = "${var.enabled?1:0}"
  checksum = "${sha1("${data.template_file.config_data.rendered}")}"
  configmap_name = "ad-manager-${local.checksum}"
}

data "template_file" "config_data" {
  template = "${file("${local.config_file_path}")}"
  vars {
    metric_consumer_bootstrap_servers = "${var.metric_consumer_bootstrap_servers}"
    metric_consumer_group_id = "${var.metric_consumer_group_id}"
    metric_consumer_topic = "${var.metric_consumer_topic}"
    metric_consumer_key_deserializer = "${var.metric_consumer_key_deserializer}"
    metric_consumer_value_deserializer = "${var.metric_consumer_value_deserializer}"
    anomaly_producer_bootstrap_servers = "${var.anomaly_producer_bootstrap_servers}"
    anomaly_producer_client_id = "${var.anomaly_producer_client_id}"
    anomaly_producer_outlier_topic = "${var.anomaly_producer_outlier_topic}"
    anomaly_producer_breakout_topic = "${var.anomaly_producer_breakout_topic}"
    anomaly_producer_key_serializer = "${var.anomaly_producer_key_serializer}"
    anomaly_producer_value_serializer = "${var.anomaly_producer_value_serializer}"
    modelservice_base_uri = "${var.modelservice_base_uri}"
    graphite_base_uri = "${var.graphite_base_uri}"
    graphite_data_retrieval_key = "${var.graphite_data_retrieval_key}"
    throttle_gate_likelihood = "${var.throttle_gate_likelihood}"
    detector_refresh_period = "${var.detector_refresh_period}"
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
    graphite_port = "${var.graphite_port}"
    graphite_host = "${var.graphite_hostname}"
    graphite_prefix = "${var.graphite_prefix}"
    env_vars = "${indent(9,"${var.env_vars}")}"
  }
}

resource "kubernetes_config_map" "ad-manager-config" {
  metadata {
    name = "${local.configmap_name}"
    namespace = "${var.namespace}"
  }
  data {
    "ad-manager.conf" = "${data.template_file.config_data.rendered}"
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
