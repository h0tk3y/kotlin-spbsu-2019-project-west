plugins {
    kotlin("jvm") version "1.3.50"
}

val ktor_version = "1.2.4"

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    testImplementation(kotlin("test"))
    testImplementation(kotlin("test-junit"))

    implementation("io.ktor:ktor-client-websockets:$ktor_version")
    implementation("io.ktor:ktor-client-cio:$ktor_version")
    implementation("io.ktor:ktor-client-js:$ktor_version")
    implementation("io.ktor:ktor-client-okhttp:$ktor_version")
    implementation("com.beust:klaxon:5.0.13")
    implementation("com.googlecode.lanterna:lanterna:3.0.1")

    implementation(project(":core"))
    testImplementation(project(":server"))
}