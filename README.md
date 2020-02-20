[![Build Status](https://travis-ci.org/ExpediaDotCom/adaptive-alerting.svg?branch=master)](https://travis-ci.org/ExpediaDotCom/adaptive-alerting)
[![codecov](https://codecov.io/gh/ExpediaDotCom/adaptive-alerting/branch/master/graph/badge.svg)](https://codecov.io/gh/ExpediaDotCom/adaptive-alerting)
[![License](https://img.shields.io/badge/license-Apache%20License%202.0-blue.svg)](https://github.com/ExpediaDotCom/adaptive-alerting/blob/master/LICENSE)

# Adaptive Alerting (AA)

Streaming anomaly detection with automated model selection and fitting.

[Wiki documentation](https://github.com/ExpediaDotCom/adaptive-alerting/wiki)

## Build

To build the Maven project: 

```
$ ./mvnw clean verify
```

### How the Travis CI build works

We use Travis CI to build AA Docker images and push them to Docker Hub. Here's how it works:

- A developer pushes a branch (`master` or otherwise) to GitHub.
- GitHub kicks off a Travis CI build.
- Travis CI reads `.travis.yml`, which drives the build.
- `.travis.yml` invokes the top-level `Makefile`.
- The top-level `Makefile`
  - runs a Maven build for the whole project
  - invokes module-specific `Makefile`s to handle building and releasing Docker images
- Each module-specific `Makefile` runs one or more module-specific build scripts to
  - build the Docker images
  - release the Docker images
- For the release (docker push), the module-specific build script delegates to the shared
  `scripts/publish-to-docker-hub.sh` script. This script has logic to push the image to Docker Hub
  if and only if the current branch is the `master`.

