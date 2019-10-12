#!/bin/bash

./gradlew serverjar
java -jar ./build/libs/server.jar
