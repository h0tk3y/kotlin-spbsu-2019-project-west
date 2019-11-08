plugins {
    kotlin("jvm") version "1.3.50"
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    testImplementation(kotlin("test"))
    testImplementation(kotlin("test-junit"))
    implementation("com.beust:klaxon:5.0.13")
}