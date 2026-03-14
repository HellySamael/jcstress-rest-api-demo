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


    // PostgreSQL driver (used by the REST server in production mode)
    implementation("org.postgresql:postgresql:42.7.4")

    // HikariCP connection pool
    implementation("com.zaxxer:HikariCP:5.0.1")

    // JCStress (concurrency testing)
    jcstressImplementation("org.openjdk.jcstress:jcstress-core:0.16")
    jcstressAnnotationProcessor("org.openjdk.jcstress:jcstress-core:0.16")

    // Unit testing (JUnit Jupiter)
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")

    // Ensure JUnit Platform launcher and Jupiter engine are available at test runtime
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.10.2")
}

jcstress {
    // Basic configuration for quick tests
    mode = "quick"
    forks = "1" // Number of JVM forks for each test
    iterations = "5" // Number of iterations per test
    jvmArgs = "-Xmx1g" // Max 1GB heap for test JVMs
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

tasks.test {
    useJUnitPlatform()
}
