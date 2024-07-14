plugins {
    kotlin("jvm") version "2.0.0"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("dev.langchain4j:langchain4j-open-ai:0.32.0")
    implementation("dev.langchain4j:langchain4j:0.32.0")
    implementation("dev.langchain4j:langchain4j-embeddings-all-minilm-l6-v2:0.32.0")
    implementation("dev.langchain4j:langchain4j-easy-rag:0.32.0")
    implementation("ch.qos.logback:logback-classic:1.4.7")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}