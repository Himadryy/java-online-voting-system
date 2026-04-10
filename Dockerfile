FROM gradle:8.5-jdk17 AS build
WORKDIR /app
# Copy the gradle wrapper and build files
COPY build.gradle settings.gradle gradlew ./
COPY gradle ./gradle
# Copy the source code
COPY src ./src

# Build the application
RUN ./gradlew build -x test --no-daemon

# Use a lightweight JRE image for the final container
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copy the built JAR from the build stage
COPY --from=build /app/build/libs/*SNAPSHOT.jar app.jar

# Expose port 8080
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
