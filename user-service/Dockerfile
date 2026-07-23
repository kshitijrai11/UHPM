# Stage 1: Build the application
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder

WORKDIR /app

# Copy the parent pom and the shared library
COPY pom.xml .
COPY shared/pom.xml shared/
COPY shared/src shared/src/
COPY shared/proto-defs shared/proto-defs/

# Copy the user-service source code
COPY user-service/pom.xml user-service/
COPY user-service/src user-service/src/

# Build the application
RUN mvn clean package -pl shared,user-service -am -DskipTests

# Stage 2: Create the runtime image
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copy the built jar file from the builder stage
COPY --from=builder /app/user-service/target/user-service-*.jar app.jar

# Expose HTTP and gRPC ports
EXPOSE 8201
EXPOSE 9091

# Set environment variables for Java tuning
ENV JAVA_OPTS="-XX:+UseZGC -XX:+ZGenerational"

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
