# Stage 1: Build the application with Maven
FROM maven:3.9.4-eclipse-temurin-17 AS build


# Set the working directory
WORKDIR /app

# Copy the pom.xml and source code
COPY pom.xml /app/
COPY src /app/src/

# Package the application (with the provided or default app name)
RUN mvn clean package -DskipTests

# Stage 2: Run the application
FROM openjdk:17-jdk-slim

# Set the working directory
WORKDIR /app

# Use the ARG APP_NAME from the first stage
COPY --from=build /app/target/*.jar /app/application.jar

# Expose the application port
EXPOSE ${CONFIG_SERVER_PORT}

# Run the application using the app name
ENTRYPOINT ["java", "-jar", "/app/application.jar"]
