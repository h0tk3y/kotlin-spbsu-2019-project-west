import org.gradle.api.tasks.testing.logging.TestExceptionFormat

plugins {
    application
    kotlin("jvm") version "1.3.50"
    id("com.github.johnrengelman.shadow") version "5.1.0"
    jacoco
}

val ktor_version = "1.2.4"

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    testImplementation(kotlin("test"))
    testImplementation(kotlin("test-junit"))

    implementation("io.ktor:ktor-client-core:$ktor_version")
    implementation("io.ktor:ktor-client-apache:$ktor_version")
    implementation("io.ktor:ktor-client-json-jvm:$ktor_version")
    implementation("io.ktor:ktor-client-jackson:$ktor_version")
    implementation("com.googlecode.lanterna:lanterna:3.0.1")
    implementation("com.xenomachina:kotlin-argparser:2.0.7")

    implementation("com.h2database:h2:1.4.200")

    implementation("org.jetbrains.exposed", "exposed-core", "0.18.1")
    implementation("org.jetbrains.exposed", "exposed-dao", "0.18.1")
    implementation("org.jetbrains.exposed", "exposed-jdbc", "0.18.1")
    implementation("org.jetbrains.exposed", "exposed-jodatime", "0.18.1")

    implementation(project(":core"))
    testImplementation(project(":server"))
}


jacoco {
    toolVersion = "0.8.4"
}

tasks.jacocoTestReport {
    reports {
        xml.isEnabled = true
        csv.isEnabled = true
        html.isEnabled = true
    }
}

tasks.test {
    finalizedBy(tasks.jacocoTestReport)
}

application {
    mainClassName = "snailmail.client.MainKt"
}