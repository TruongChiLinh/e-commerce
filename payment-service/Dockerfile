FROM maven:3.8.4-openjdk-17 AS builder

WORKDIR /app

# Copy common-lib
COPY common-lib/pom.xml common-lib/pom.xml
COPY common-lib/src common-lib/src

# Build common-lib first
RUN cd common-lib && mvn clean install -DskipTests

# Copy service files
COPY payment-service/pom.xml .
COPY payment-service/src ./src

# Build service
RUN mvn clean package -DskipTests

# Runtime stage
FROM openjdk:17-jdk-slim

WORKDIR /app

# Copy built jar
COPY --from=builder /app/target/payment-service-1.0.0.jar app.jar

EXPOSE 8085

CMD ["java", "-Xmx512m", "-XX:+UseContainerSupport", "-jar", "app.jar"]