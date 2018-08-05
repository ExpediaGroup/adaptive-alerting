# Aquila

Aquila is an anomaly detection method for time series with daily and weekly seasonalities. It involves:

- a predictive model for mean estimates
- a predictive model for dispersion
- a classification model for anomaly detection, based on the predictive models

We currently implement this as a single Dockerized web service with one endpoint for training and another for performing
the actual anomaly detection. But this module is in transition. Here's what to expect:

- Aquila will go back into its own git repo.
- The project will have three separate modules:
  - core: Core library, shared by both the trainer and detector.
  - train: Web service to train Aquila models. This will have its own Spring Boot app and Docker image.
  - detect: Web service to run anomaly detection on time series data. This will have its own Spring
    Boot app and Docker image.

## Building the app

### with Maven

```
$ mvn clean verify
```

### with Docker

```
$ ./build.sh -p    # builds Spring Boot fat JAR
$ ./build.sh -b    # builds Docker image for the Spring Boot fat JAR
```

## Running the app

### with Maven

```
$ mvn spring-boot:run
```

### with Docker

```
$ ./build.sh -r
```
