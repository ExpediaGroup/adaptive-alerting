.PHONY: all release

PWD := $(shell pwd)
MAVEN := ./mvnw

clean:
	${MAVEN} clean

build:
	${MAVEN} install package

all:
	clean
	build

# build all and release
release:
	all
	cd aquila && $(MAKE) release
	cd kafka && $(MAKE) release
