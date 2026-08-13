# --- Stage 1: Build the Jar ---
FROM gradle:9.5.1-jdk AS build
WORKDIR /app

# Copy dependency files first for layer caching
COPY build.gradle.kts settings.gradle.kts gradle.properties ./
COPY gradle ./gradle

# Copy source code and build shadow/fat JAR
COPY src ./src
RUN gradle shadowJar --no-daemon

# --- Stage 2: Minimal Runtime ---
FROM amazoncorretto:21-alpine
WORKDIR /app

# Copy compiled JAR from build stage
COPY --from=build /app/build/libs/*.jar app.jar

# Expose Ktor port
EXPOSE 8080

# Environment variables with sensible AWS production defaults
ENV PORT=8080
ENV AWS_REGION=eu-west-2
ENV DYNAMODB_TABLE=UrlMappings
ENV DYNAMODB_LOCAL=false

ENTRYPOINT ["java", "-jar", "app.jar"]