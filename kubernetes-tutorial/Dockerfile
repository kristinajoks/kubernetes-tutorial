# syntax=docker/dockerfile:1

##########
# Build
##########
FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace

# Copy Gradle wrapper first for better layer caching
COPY gradlew gradlew
COPY gradle  gradle
COPY build.gradle settings.gradle ./

# Handle Windows line-endings and ensure wrapper is executable
RUN sed -i 's/\r$//' gradlew && chmod +x gradlew

# Pre-fetch deps (optional but speeds up)
RUN ./gradlew --no-daemon dependencies || true

# Copy sources last
COPY src src

# Build the jar (skip tests for speed; remove -x test if you want)
RUN ./gradlew --no-daemon clean installDist

##########
# Runtime
##########
FROM eclipse-temurin:21-jre
WORKDIR /app

# Copy the built jar (wildcard handles any artifact name)
COPY --from=build /workspace/build/install/beverage-service/ /app/

# Expose the HTTP port your app listens on
EXPOSE 8080

# Extra JVM options can be passed via JAVA_OPTS at runtime
ENV PORT=8080

ENTRYPOINT ["/app/bin/beverage-service"]
