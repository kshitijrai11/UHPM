# Stage 1: Build the application
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder

WORKDIR /app

# Copy the parent pom and the shared library (since product-service depends on it)
COPY pom.xml .
COPY shared/pom.xml shared/
COPY shared/src shared/src/
COPY shared/proto-defs shared/proto-defs/

# Copy the product-service source code
COPY product-service/pom.xml product-service/
COPY product-service/src product-service/src/

# Build the application
RUN mvn clean package -pl shared,product-service -am -DskipTests

# Stage 2: Create the runtime image
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copy the built jar file from the builder stage
COPY --from=builder /app/product-service/target/product-service-*.jar app.jar

# Expose HTTP and gRPC ports
EXPOSE 8081
EXPOSE 9090

# Set environment variables for Java tuning (optional but recommended for containers)
ENV JAVA_OPTS="-XX:+UseZGC -XX:+ZGenerational"

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
