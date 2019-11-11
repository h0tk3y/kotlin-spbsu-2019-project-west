plugins {
    application
    kotlin("jvm") version "1.3.50"
}

val ktor_version = "1.2.4"

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

    implementation(project(":core"))
    testImplementation(project(":client"))
}

application {
    mainClassName = "snailmail.server.MainKt"
}