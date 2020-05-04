# ------------------- Deployment ------------------- #

kind: Deployment
apiVersion: apps/v1beta2
metadata:
  labels:
    k8s-app: ${app_name}
  name: ${app_name}
  namespace: ${namespace}
spec:
  replicas: ${replicas}
  revisionHistoryLimit: 10
  selector:
    matchLabels:
      k8s-app: ${app_name}
  template:
    metadata:
      labels:
        k8s-app: ${app_name}
    spec:
      containers:
      - name: ${app_name}
        image: ${image}
        imagePullPolicy: ${image_pull_policy}
        volumeMounts:
        - mountPath: /config/visualizer.conf
          subPath: visualizer.conf
          name: config-volume
        - mountPath: /config/functions.txt
          subPath: functions.txt
          name: functions-volume
        resources:
          limits:
            cpu: ${cpu_limit}
            memory: ${memory_limit}Mi
          requests:
            cpu: ${cpu_request}
            memory: ${memory_request}Mi
        env:
        - name: "AA_OVERRIDES_CONFIG_PATH"
          value: "/config/aa-metric-functions.conf"
        - name: "AA_GRAPHITE_HOST"
          value: "${graphite_host}"
        - name: "AA_GRAPHITE_PORT"
          value: "${graphite_port}"
        - name: "AA_GRAPHITE_ENABLED"
          value: "${graphite_enabled}"
        - name: "GRAPHITE_PREFIX"
          value: "${graphite_prefix}"
        - name: "JAVA_XMS"
          value: "${jvm_memory_limit}m"
        - name: "JAVA_XMX"
          value: "${jvm_memory_limit}m"
        ${env_vars}
        # FIXME Reinstate after removing haystack-commons dependency
#        livenessProbe:
#          exec:
#            command:
#            - grep
#            - "true"
#            - /app/isHealthy
#          initialDelaySeconds: 30
#          periodSeconds: 5
#          failureThreshold: 6
      # Add initContainer to download input file to /config/functions.txt
      initContainers:
      - name: download-input-file
        # example - image: busybox
        image: ${initContainer_image}
        # example - command: ["sh", "-c", "curl", "<input-file-location>", "-o", "/config/functions.txt"]
        command: ${download_input_file_command}
        volumeMounts:
        - mountPath:  /config
          name: functions-volume
      nodeSelector:
        ${node_selector_label}
      volumes:
      - name: config-volume
        configMap:
          name: ${configmap1_name}
      - name: functions-volume
        emptyDir: {}
