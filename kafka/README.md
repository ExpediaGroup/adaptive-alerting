# Adaptive Alerting - Kafka


## Running KStream apps in docker

Need to package the app (if other modules have been updated those will need to be re-built with `mvn install`):

```bash
mvn clean package
```

Build the docker image (use the appropriate script for the app):

```bash
docker/scripts/build-{APP_NAME e.g. constant-detector}.sh
```

Run pre-built docker image:

```bash
docker run {APP_NAME}
```

To do all these steps: Run maven package, build the docker image and run it (use the appropriate script for the app):
   
```bash
docker/scripts/build-{APP_NAME}.sh -p -r
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

you may also need something to listen on that port so you can run:

```bash
python -m SimpleHTTPServer 2003
```
