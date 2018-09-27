.PHONY: integration_test release

MAVEN := ../mvnw

docker_build:
	./build.sh -b

integration_test:
	${MAVEN} test

# build jar, docker image and run integration tests
all: docker_build integration_test

# build all and publish docker images
release: docker_build
	./build.sh --release
