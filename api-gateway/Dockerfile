# Stage 1: Build the application
FROM maven:3.9.6-eclipse-temurin-21 AS builder

WORKDIR /app

# Copy all source code
COPY . .

# Build the application
RUN mvn clean package -pl shared,api-gateway -am -DskipTests

# Stage 2: Create the runtime image
FROM eclipse-temurin:21-jre

WORKDIR /app

# Copy the built jar file from the builder stage
COPY --from=builder /app/api-gateway/target/api-gateway-*.jar app.jar

# Expose HTTP port
EXPOSE 8080

# Set environment variables for Java tuning
ENV JAVA_OPTS="-XX:+UseZGC -XX:+ZGenerational"

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
