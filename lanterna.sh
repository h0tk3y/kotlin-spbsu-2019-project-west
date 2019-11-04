#!/bin/bash

cd "$(dirname "$0")"
./gradlew lanternaClientJar && java -jar ./build/libs/lanternaClient.jar $@