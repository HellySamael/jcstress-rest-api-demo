# ── Stage 1: build ────────────────────────────────────────────────────────────
FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /app

# Copy Gradle wrapper + config first (layer cache)
COPY gradlew gradlew.bat settings.gradle.kts build.gradle.kts gradle.properties ./
COPY gradle/ gradle/

RUN chmod +x gradlew

# Download dependencies (cached layer)
RUN ./gradlew dependencies --no-daemon -q || true

# Copy source and build the fat jar
COPY src/ src/
RUN ./gradlew jar --no-daemon -x test -x jcstress

# ── Stage 2: runtime ──────────────────────────────────────────────────────────
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

COPY --from=builder /app/build/libs/DEVOXX-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 7070

ENTRYPOINT ["java", "-jar", "app.jar"]

