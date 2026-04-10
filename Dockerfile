# syntax=docker/dockerfile:1

# Build frontend assets
FROM node:20-alpine AS frontend-build
WORKDIR /app/frontend
COPY frontend/package*.json ./
RUN npm ci
COPY frontend/ .
RUN npm run build

# Build Spring Boot jar with frontend assets included
FROM maven:3.9.8-eclipse-temurin-17 AS backend-build
WORKDIR /app
COPY pom.xml mvnw mvnw.cmd ./
COPY .mvn .mvn
COPY src src
COPY frontend/dist src/main/resources/static
RUN chmod +x mvnw && ./mvnw clean package -DskipTests

# Runtime image
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=backend-build /app/target/skillbridge-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
