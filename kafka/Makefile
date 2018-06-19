.PHONY: integration_test release

MAVEN := ../mvnw

docker_build:
	./docker/scripts/build-metric-router.sh -b	
	./docker/scripts/build-constant-detector.sh -b
	./docker/scripts/build-ewma-detector.sh -b
	./docker/scripts/build-pewma-detector.sh -b

integration_test:
	${MAVEN} test

# build jar, docker image and run integration tests
all: docker_build integration_test

# build all and publish docker images
release: docker_build
	./docker/scripts/build-metric-router.sh --release
	./docker/scripts/build-constant-detector.sh --release
	./docker/scripts/build-ewma-detector.sh --release
	./docker/scripts/build-pewma-detector.sh --release

