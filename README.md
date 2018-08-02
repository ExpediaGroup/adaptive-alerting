[![Build Status](https://travis-ci.org/ExpediaDotCom/haystack-adaptive-alerting.svg?branch=master)](https://travis-ci.org/ExpediaDotCom/haystack-adaptive-alerting)
[![License](https://img.shields.io/badge/license-Apache%20License%202.0-blue.svg)](https://github.com/ExpediaDotCom/haystack-adaptive-alerting/blob/master/LICENSE)
[![Join the chat at https://gitter.im/expedia-haystack/Lobby](https://badges.gitter.im/expedia-haystack/Lobby.svg)](https://gitter.im/expedia-haystack/Lobby?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

# Adaptive Alerting (AA)

Streaming anomaly detection with automated model selection.

[Wiki documentation](https://github.com/ExpediaDotCom/haystack-adaptive-alerting/wiki)

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
