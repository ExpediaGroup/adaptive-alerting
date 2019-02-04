.PHONY: integration_test release

MAVEN := ../mvnw

docker_build:
	./docker/scripts/build-ad-mapper.sh -b
	./docker/scripts/build-ad-manager.sh -b
	./docker/scripts/build-mc-a2m-mapper.sh -b
	./docker/scripts/build-a2a-mapper.sh -b

integration_test:
	${MAVEN} test

# build jar, docker image and run integration tests
all: docker_build integration_test

# build all and publish docker images
release: docker_build
	./docker/scripts/build-ad-mapper.sh --release
	./docker/scripts/build-ad-manager.sh --release
	./docker/scripts/build-mc-a2m-mapper.sh --release
