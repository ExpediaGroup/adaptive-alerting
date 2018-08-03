# Aquila

This module is in transition. Here's what to expect:

- Aquila will go back into its own git repo.
- The project will have three separate modules:
  - core: Shared library
  - train: Web service to train Aquila models. This will have its own Spring Boot app and Docker image.
  - detect: Web service to run anomaly detection on time series data. This will have its own Spring
    Boot app and Docker image.
