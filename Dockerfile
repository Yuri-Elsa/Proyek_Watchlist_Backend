# ── Stage 1: Build ────────────────────────────────────────────────────────────
FROM gradle:8.8-jdk17 AS builder

WORKDIR /app
COPY build.gradle.kts settings.gradle.kts gradle.properties ./
COPY src ./src

# Download dependencies first (layer cache)
RUN gradle dependencies --no-daemon || true

# Build fat JAR
RUN gradle buildFatJar --no-daemon

# ── Stage 2: Runtime ──────────────────────────────────────────────────────────
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copy the assembled JAR
COPY --from=builder /app/build/libs/*-all.jar app.jar

# Create uploads directory
RUN mkdir -p uploads/todos uploads/users

EXPOSE 8080

ENV PORT=8080

ENTRYPOINT ["java", "-jar", "app.jar"]
