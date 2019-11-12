plugins {
    kotlin("jvm") version "1.3.50"
}

val jackson_version = "2.10.0"

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("reflect"))
    implementation("com.fasterxml.jackson.core:jackson-annotations:$jackson_version")
    implementation("com.fasterxml.jackson.core:jackson-databind:$jackson_version")
    testImplementation(kotlin("test"))
    testImplementation(kotlin("test-junit"))
}