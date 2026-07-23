# Stage 1: Build the application
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder

WORKDIR /app

# Copy the parent pom and the shared library
COPY pom.xml .
COPY shared/pom.xml shared/
COPY shared/src shared/src/
COPY shared/proto-defs shared/proto-defs/

# Copy the order-service source code
COPY order-service/pom.xml order-service/
COPY order-service/src order-service/src/

# Build the application
RUN mvn clean package -pl shared,order-service -am -DskipTests

# Stage 2: Create the runtime image
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copy the built jar file from the builder stage
COPY --from=builder /app/order-service/target/order-service-*.jar app.jar

# Expose HTTP and gRPC ports
EXPOSE 8203
EXPOSE 9093

# Set environment variables for Java tuning
ENV JAVA_OPTS="-XX:+UseZGC -XX:+ZGenerational"

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
