import org.gradle.api.tasks.testing.logging.TestExceptionFormat

plugins {
    application
    kotlin("jvm") version "1.3.50"
    jacoco
}

val ktor_version = "1.2.4"
val jackson_databind_version = "2.10.0"

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    testImplementation(kotlin("test"))
    testImplementation(kotlin("test-junit"))

    implementation("io.ktor:ktor-server-netty:$ktor_version")
    implementation("io.ktor:ktor-jackson:$ktor_version")
    implementation("io.ktor:ktor-auth:$ktor_version")
    implementation("io.ktor:ktor-auth-jwt:$ktor_version")
    implementation("com.xenomachina:kotlin-argparser:2.0.7")
    implementation("ch.qos.logback:logback-classic:1.2.3")

    implementation("com.h2database:h2")

    implementation("org.jetbrains.exposed", "exposed-core", "0.18.1")
    implementation("org.jetbrains.exposed", "exposed-dao", "0.18.1")
    implementation("org.jetbrains.exposed", "exposed-jdbc", "0.18.1")
    implementation("org.jetbrains.exposed", "exposed-jodatime", "0.18.1")

    implementation("joda-time:joda-time:2.10.5")

    testCompile("io.ktor:ktor-server-test-host:$ktor_version")

    implementation(project(":core"))
    testImplementation(project(":client"))
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
    mainClassName = "snailmail.server.MainKt"
}