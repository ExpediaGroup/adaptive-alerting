.PHONY: all release

PWD := $(shell pwd)
MAVEN := ./mvnw

clean:
	${MAVEN} clean

build:
	${MAVEN} install package

all: clean build

docker_build:
	cd modelservice && $(MAKE) docker_build && cd ..
	cd kafka && $(MAKE) docker_build && cd ..
	cd notifier && $(MAKE) docker_build && cd ..

# build all and release
release: all
	cd modelservice && $(MAKE) release && cd ..
	cd kafka && $(MAKE) release && cd ..
	cd notifier && $(MAKE) release && cd ..
