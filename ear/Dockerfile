# --- 1. STAGE: Build

FROM maven:3.9-eclipse-temurin-21 AS build

# Current working directory
WORKDIR /app

# Copy Maven configuration files to cache dependencies
COPY pom.xml .
# Pre-download all dependencies - offline becase of possible network issues
RUN mvn dependency:go-offline

# Copy the source code
COPY src ./src

# Build the application and package it into a .jar file
# Skip tests for faster build
RUN mvn clean package -DskipTests

# --- 2. STAGE: Run ---
# Light weight Java runtime image
FROM eclipse-temurin:21-jre

WORKDIR /app

# Copy the built .jar file from the build stage
COPY --from=build /app/target/*.jar app.jar

# Port on which the application will run
EXPOSE 8080

# Command to run the application
ENTRYPOINT ["java", "-jar", "app.jar"]