#!/bin/bash

cd "$(dirname "$0")"
./gradlew clientjar && java -jar ./build/libs/client.jar