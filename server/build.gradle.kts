plugins {
    application
    kotlin("jvm") version "1.3.50"
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

    testCompile("io.ktor:ktor-server-test-host:$ktor_version")

    implementation(project(":core"))
    testImplementation(project(":client"))
}

application {
    mainClassName = "snailmail.server.MainKt"
}