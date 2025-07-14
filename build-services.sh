#!/bin/bash

set -e

echo "🚀 Building all microservices..."

# Build common-lib first
echo "📦 Building common-lib..."
cd common-lib
mvn clean install -DskipTests
cd ..

# Build each service
services=("auth-service" "user-service" "product-service" "order-service" "payment-service")

for service in "${services[@]}"; do
    echo "🔨 Building $service..."
    cd $service
    mvn clean package -DskipTests
    cd ..
    echo "✅ $service built successfully!"
done

echo "🎉 All services built successfully!"
echo "💡 Now you can run: docker-compose up -d"