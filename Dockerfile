# ---- Stage 1: Build ----
FROM eclipse-temurin:17-jdk AS builder

WORKDIR /app

# Copy gradle files first for layer caching
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

# Make gradlew executable
RUN chmod +x gradlew

# Download dependencies first (cached layer)
RUN ./gradlew dependencies --no-daemon || true

# Copy source code
COPY src src

# Build the application
RUN ./gradlew clean bootJar -x check -x test --no-daemon

# ---- Stage 2: Run ----
FROM eclipse-temurin:17-jre

WORKDIR /app

# Copy the built jar from the builder stage
COPY --from=builder /app/build/libs/*.jar app.jar

# Use PORT env var from Railway, default to 8080
EXPOSE 8080

# Run with memory limits to keep Railway costs low (~$2-3/month)
ENTRYPOINT ["java", "-Xmx256m", "-Xms128m", "-XX:TieredStopAtLevel=1", "-Xss512k", "-jar", "app.jar"]
