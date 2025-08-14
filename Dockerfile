# Multi-stage build for Spring Boot application
FROM maven:3.9.5-openjdk-17 AS build

WORKDIR /app
COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests

# Runtime stage
FROM openjdk:17-jre-slim

WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
