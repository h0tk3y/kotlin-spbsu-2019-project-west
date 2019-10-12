#!/bin/bash

./gradlew clientjar
java -jar ./build/libs/client.jar
