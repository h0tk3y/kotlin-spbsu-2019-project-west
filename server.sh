#!/bin/bash

cd "$(dirname "$0")"
./gradlew serverjar
java -jar ./build/libs/server.jar
