.PHONY: all release

PWD := $(shell pwd)
MAVEN := ./mvnw

clean:
	${MAVEN} clean

build:
	${MAVEN} install package

all: clean build

# build all and release
release: all
	cd kafka && $(MAKE) release
	./.travis/deploy.sh



