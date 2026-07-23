FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Add a non-root user
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copy the built jar
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar

# Add healthcheck
HEALTHCHECK --interval=30s --timeout=3s \
  CMD wget -q -O - http://config:ultra2025@localhost:8888/actuator/health || exit 1

EXPOSE 8888

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
