plugins {
    kotlin("jvm") version "1.3.50"
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("reflect"))
    implementation("com.fasterxml.jackson.core:jackson-annotations:2.9.0")
    testImplementation(kotlin("test"))
    testImplementation(kotlin("test-junit"))
}