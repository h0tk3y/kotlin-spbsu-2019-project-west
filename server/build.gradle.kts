import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import de.gesellix.gradle.docker.tasks.*

plugins {
    application
    kotlin("jvm") version "1.3.50"
    id("de.gesellix.docker") version "2019-06-28T09-51-58"
    jacoco
}

val ktor_version = "1.2.4"
val jackson_databind_version = "2.9.9"

dependencies {
    implementation(kotlin("stdlib-jdk8"))

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.3.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.3.1")

    implementation("io.ktor:ktor-server-netty:$ktor_version")
    implementation("io.ktor:ktor-jackson:$ktor_version")
    implementation("io.ktor:ktor-auth:$ktor_version")
    implementation("io.ktor:ktor-auth-jwt:$ktor_version")
    implementation("com.xenomachina:kotlin-argparser:2.0.7")
    implementation("ch.qos.logback:logback-classic:1.2.3")

    implementation("com.h2database:h2:1.4.200")

    implementation("org.jetbrains.exposed", "exposed-core", "0.18.1")
    implementation("org.jetbrains.exposed", "exposed-dao", "0.18.1")
    implementation("org.jetbrains.exposed", "exposed-jdbc", "0.18.1")
    implementation("org.jetbrains.exposed", "exposed-jodatime", "0.18.1")

    implementation("joda-time:joda-time:2.10.5")

    testCompile("io.ktor:ktor-server-test-host:$ktor_version")

    implementation(project(":core"))
    testImplementation(project(":client"))
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

docker {
    dockerHost = "unix:///var/run/docker.sock"
}

val groupDocker = "docker"
val dockerRegistry = "registry.promoatlas.ru"
val productName = "snailmail-west"
val dockerImageName = "kotlin-spbsu/$productName"
val dockerImageAndTag = "$dockerImageName:$version"
val remoteDockerImageAndTag = "$dockerImageName:$version"

val rmDockerImage = tasks.register<DockerRmiTask>("rmDockerImage") {
    group = groupDocker
    imageId = dockerImageAndTag
}

val buildLocalDockerImage = tasks.register<DockerBuildTask>("buildLocalDockerImage") {
    dependsOn("installDist")
    group = groupDocker
    //buildParams = {"nocache" to true}
    imageName = dockerImageAndTag
    setBuildContextDirectory (file("."))
    doFirst {
        println("Building docker image $imageName ...")
    }
}

val tagDockerImage = tasks.register<DockerTagTask>("tagDockerImage") {
    dependsOn(buildLocalDockerImage)
    group = groupDocker
    imageId = dockerImageAndTag
    tag = remoteDockerImageAndTag
    doFirst {
        println("Tag docker image $imageId as $tag ...")
    }
}

val pushDockerImageToRegistry = tasks.register<DockerPushTask>("pushDockerImageToRegistry") {
    dependsOn(tagDockerImage)
            group = groupDocker
    repositoryName = remoteDockerImageAndTag
    registry = dockerRegistry
    doFirst {
        println("Pushing image $repositoryName to registry $registry ...")
    }
}

val removeLocalDockerImages = tasks.register<GenericDockerTask>("removeLocalDockerImages") {
    group = groupDocker
    doFirst {
        println("Removing local images : $dockerImageAndTag, $remoteDockerImageAndTag and $dockerRegistry/$remoteDockerImageAndTag ...")
    }
    doLast {
        val docker = dockerClient
        docker.rmi(dockerImageAndTag)
        docker.rmi(remoteDockerImageAndTag)
        docker.rmi("$dockerRegistry/$remoteDockerImageAndTag")
    }
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
    useJUnitPlatform()
}

application {
    mainClassName = "snailmail.server.MainKt"
}