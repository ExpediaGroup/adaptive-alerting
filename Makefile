.PHONY: all release

PWD := $(shell pwd)
MAVEN := ./mvnw

clean:
	${MAVEN} clean

build: clean
	${MAVEN} install package

all: clean

# build all and release
release: all
	cd kafka && $(MAKE) release
	./.travis/deploy.sh



