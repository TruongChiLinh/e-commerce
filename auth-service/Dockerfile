FROM maven:3.8.4-openjdk-17 AS builder

# Fix DNS issue
RUN echo "nameserver 8.8.8.8" > /etc/resolv.conf

WORKDIR /app

# Copy common-lib
COPY common-lib/pom.xml common-lib/pom.xml
COPY common-lib/src common-lib/src

# Build common-lib first
RUN cd common-lib && mvn clean install -DskipTests

# Copy service files
COPY auth-service/pom.xml .
COPY auth-service/src ./src

# Build service
RUN mvn clean package -DskipTests

# Runtime stage
FROM openjdk:17-jdk-slim

# Install curl for health checks
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Copy built jar
COPY --from=builder /app/target/auth-service-1.0.0.jar app.jar

EXPOSE 8084

CMD ["java", "-Xmx512m", "-XX:+UseContainerSupport", "-jar", "app.jar"]