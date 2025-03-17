# Build Stage
FROM maven:3.8.4-openjdk-17-slim AS build

# Set the working directory inside the container
WORKDIR /app

# Copy the source code into the container
COPY . .

# Run Maven to build the JAR file
RUN mvn clean package

# Runtime Stage
FROM openjdk:17-jdk-slim

# Set the working directory inside the container
WORKDIR /app

# Copy the JAR file built in the 'build' stage
COPY --from=build /app/target/watch-later-spring-1.0.0.jar /app/app.jar

# Expose the port your Spring Boot app will run on (default is 8080)
EXPOSE 8080

# Run the Spring Boot application
ENTRYPOINT ["java", "-jar", "app.jar"]
