# ========================================================= BUILD \/

FROM eclipse-temurin:25-jdk AS build

WORKDIR /app

# Copy Gradle wrapper and build configuration first to take advantage of Docker layer caching.
COPY gradlew .
COPY gradle ./gradle
COPY build.gradle.kts .
COPY settings.gradle.kts .

RUN chmod +x gradlew

# Copy source
COPY src ./src

# Build the application
RUN ./gradlew buildFatJar --no-daemon

# ========================================================= BUILD /\

# ========================================================= RUNTIME \/
FROM eclipse-temurin:25-jre

WORKDIR /app

COPY --from=build /app/build/libs/*-all.jar app.jar

EXPOSE 8080
EXPOSE 9090

ENTRYPOINT ["java", "-jar", "app.jar"]

# ========================================================= RUNTIME /\