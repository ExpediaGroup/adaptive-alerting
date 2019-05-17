locals {
  app_name = "modelservice"
  deployment_yaml_file_path = "${path.module}/templates/deployment_yaml.tpl"
  application_yaml_file_path = "${path.module}/templates/application_yaml.tpl"
  count = "${var.enabled?1:0}"
  checksum = "${sha1("${data.template_file.config_data.rendered}")}"
  configmap_name = "modelservice-${local.checksum}"
}

data "template_file" "config_data" {
  template = "${file("${local.application_yaml_file_path}")}"
  vars {
    db_endpoint = "${var.db_endpoint}"
    graphite_url = "${var.graphite_url}"
    detector_mapper_es_urls = "${var.detector_mapper_es_urls}"
    detector_mapper_index_name = "${var.detector_mapper_index_name}"
    detector_mapper_doctype = "${var.detector_mapper_doctype}"
    detector_mapper_es_config_connection_timeout = "${var.detector_mapper_es_config_connection_timeout}"
    detector_mapper_es_config_connection_retry_timeout = "${var.detector_mapper_es_config_connection_retry_timeout}"
    detector_mapper_es_config_max_total_connection = "${var.detector_mapper_es_config_max_total_connection}"
    detector_mapper_es_config_aws_iam_auth_required = "${var.detector_mapper_es_config_aws_iam_auth_required}"
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
    service_port = "${var.service_port}"
    container_port = "${var.container_port}"
    node_selector_label = "${var.node_selector_label}"
    configmap_name = "${local.configmap_name}"

    # Environment
    jvm_memory_limit = "${var.jvm_memory_limit}"
    graphite_port = "${var.graphite_port}"
    graphite_host = "${var.graphite_hostname}"
    graphite_enabled = "${var.graphite_enabled}"
    graphite_prefix = "${var.graphite_prefix}"
    env_vars = "${indent(9,"${var.env_vars}")}"
  }
}

resource "kubernetes_config_map" "model-service-config" {
  metadata {
    name = "${local.configmap_name}"
    namespace = "${var.namespace}"
  }
  data {
    "application.yml" = "${data.template_file.config_data.rendered}"
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
