plugins {
    application
    kotlin("jvm") version "1.3.50"
    id("com.github.johnrengelman.shadow") version "5.1.0"
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
    implementation("com.googlecode.lanterna:lanterna:3.0.1")
    implementation("com.xenomachina:kotlin-argparser:2.0.7")

    implementation(project(":core"))
    testImplementation(project(":server"))
}

application {
    mainClassName = "snailmail.client.MainKt"
}