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
	cd kafka && $(MAKE) release && cd ..
	cd modelservice && $(MAKE) release && cd ..
	cd aquila/train && $(MAKE) release && cd ../..
	cd aquila/detect && $(MAKE) release && cd ../..
