import org.gradle.api.JavaVersion
import org.gradle.jvm.toolchain.JavaLanguageVersion

plugins {
    java
    application
    id("io.github.reyerizo.gradle.jcstress") version "0.9.0"
}

group = "com.example"
version = "0.0.1-SNAPSHOT"

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

application {
    mainClass.set("com.example.demo.DemoApplication")
}

dependencies {
    // Javalin (web framework)
    implementation("io.javalin:javalin-bundle:6.7.0")

    // Logging
    implementation("org.slf4j:slf4j-simple:2.0.17")

    // Dagger 2 (dependency injection)
    implementation("com.google.dagger:dagger:2.59.1")
    implementation("jakarta.inject:jakarta.inject-api:2.0.1") // Use Jakarta Inject for @Inject, @Singleton
    annotationProcessor("com.google.dagger:dagger-compiler:2.59.1")

    // JCStress (concurrency testing)
    jcstressImplementation("org.openjdk.jcstress:jcstress-core:0.16")
    jcstressAnnotationProcessor("org.openjdk.jcstress:jcstress-core:0.16")
}

jcstress {
    // Basic configuration for quick tests
    mode = "quick"
    forks = "1" // Number of JVM forks for each test
    iterations = "5" // Number of iterations per test
    jvmArgs = "-Xmx1g" // Max 1GB heap for test JVMs
    regexp = "com.example.demo.domain.*" // Run all stress tests in our package
    reportDir = layout.buildDirectory.dir("reports/jcstress").get().asFile.absolutePath
}

tasks.distZip {
    enabled = false
}

tasks.distTar {
    enabled = false
}

tasks.jcstressDistTar {
    enabled = false
}

tasks.jcstressDistZip {
    enabled = false
}
