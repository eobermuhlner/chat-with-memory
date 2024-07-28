plugins {
    kotlin("jvm") version "2.0.0"
    kotlin("plugin.spring") version "2.0.0"
    id("org.springframework.boot") version "3.1.2"
    id("io.spring.dependency-management") version "1.1.0"
}

group = "ch.obermuhlner"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("com.h2database:h2")
    implementation("org.postgresql:postgresql:42.7.3")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("dev.langchain4j:langchain4j:0.32.0")
    implementation("dev.langchain4j:langchain4j-open-ai:0.32.0")
    implementation("dev.langchain4j:langchain4j-pgvector:0.32.0")
    implementation("dev.langchain4j:langchain4j-embeddings-all-minilm-l6-v2:0.32.0")
    implementation("dev.langchain4j:langchain4j-easy-rag:0.32.0")
    implementation("org.jsoup:jsoup:1.15.4")
    implementation("org.slf4j:slf4j-api:2.0.13")
    implementation("ch.qos.logback:logback-classic:1.4.7")
    implementation("org.apache.logging.log4j:log4j-to-slf4j:2.20.0")
    implementation("org.slf4j:jcl-over-slf4j:2.0.7")
    implementation("org.slf4j:jul-to-slf4j:2.0.7")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)
}
