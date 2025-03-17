# Use an official OpenJDK runtime as the base image
FROM openjdk:17-jdk-slim
LABEL authors="HackintoshSA Team"


# Set the working directory inside the container
WORKDIR /app


# Copy the JAR file built by Maven/Gradle to the container
COPY target/*.jar app.jar

# Expose the port your Spring Boot app will run on (default is 8080)
EXPOSE 8080

# Run the Spring Boot application
ENTRYPOINT ["java", "-jar", "app.jar"]