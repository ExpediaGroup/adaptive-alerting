# Testing the Adaptive Alerting pipeline with Docker Compose

The Docker Compose setup allows you run the Adaptive Alerting components and test sample metrics against the project. You can begin by building the project's docker images through the Makefile. You can also substitute previous versions by updating the docker-compose.yml file and referencing images from Docker Hub.

#### 1. Build project images locally (From adaptive-alerting directory)
```
make clean build docker_build
```

#### 2. Run images using Docker Compose (From adaptive-alerting/docker-compose directory)
```
docker-compose up
```

#### 3. Send sample events to running environment
Wait a few minutes for docker-compose to finish and all the services to come up before executing `send-sample-metrics.py`. If no anomalies are generated it could be because the sample detector has not yet been inserted.
```
python3 send-sample-metrics.py
```

**Note**: This Python script sends randomized sample metrics into the intake Kafka topic and show any anomalous values. Anomalies are determined by the sample constant threshold detector and detector mapping that is populated upon bringing up docker-compose. Requires: [msgpack](https://msgpack.org/index.html) and [kafka-python](https://kafka-python.readthedocs.io/en/master/) libraries.

#### 4. Reset Docker Compose and remove data volumes
```
docker-compose down -v
```

#### Support Files executed by Elasticsearch container
 - `/scripts/start-es.sh`: Kicks off `populate-es.sh` in the background and brings up Elasticsearch.
 - `/scripts/populate-es.sh`: Pushes a sample detector and detector mapping to the modelservice API as soon as the API is available.