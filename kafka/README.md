# Adaptive Alerting - Kafka


## Running KStream apps in docker

Need to package the app (if other modules have been updated those will need to be re-built):

```bash
mvn clean package
```

Build the docker image:

```bash
docker build -f docker/{APP_NAME e.g. constant-detector}/Dockerfile .
```

run the docker image:

```bash
docker run $(docker images -q | head -1)
```

### Troubleshooting

If you get an error:

```java
Caused by: org.apache.kafka.common.config.ConfigException: No resolvable bootstrap urls given in bootstrap.servers
```

Try adding the following entry to your hosts file:

```bash
{YOUR_EXTERNAL_IP} kafkasvc
```


If you get an error:

```java
java.net.ConnectException: Exception connecting to HostAndPort{host='monitoring-influxdb-graphite.kube-system.svc', port=2003}
```

Try adding the following entry to your hosts file:

```bash
{YOUR_EXTERNAL_IP} monitoring-influxdb-graphite.kube-system.svc
```
