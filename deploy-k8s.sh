#!/bin/bash

echo "🚀 Deploying microservices to Kubernetes..."

# Build Docker images
echo "📦 Building Docker images..."
docker build -t auth-service:latest -f auth-service/Dockerfile .
docker build -t user-service:latest -f user-service/Dockerfile .
docker build -t product-service:latest -f product-service/Dockerfile .
docker build -t order-service:latest -f order-service/Dockerfile .
docker build -t payment-service:latest -f payment-service/Dockerfile .

# Apply Kubernetes manifests
echo "☸️ Applying Kubernetes manifests..."

# Create namespace first
kubectl apply -f k8s/namespace.yaml

# Apply ConfigMaps
kubectl apply -f k8s/configmap.yaml

# Apply MySQL
kubectl apply -f k8s/mysql.yaml

# Wait for MySQL to be ready
echo "⏳ Waiting for MySQL to be ready..."
kubectl wait --for=condition=ready pod -l app=mysql -n microservices --timeout=300s

# Apply microservices
kubectl apply -f k8s/auth-service.yaml
kubectl apply -f k8s/user-service.yaml
kubectl apply -f k8s/product-service.yaml
kubectl apply -f k8s/order-service.yaml
kubectl apply -f k8s/payment-service.yaml

# Apply Ingress
kubectl apply -f k8s/ingress.yaml

echo "✅ Deployment completed!"
echo ""
echo "📋 Check status with:"
echo "  kubectl get pods -n microservices"
echo "  kubectl get services -n microservices"
echo "  kubectl get ingress -n microservices"
echo ""
echo "🌐 Add to /etc/hosts:"
echo "  127.0.0.1 microservices.local"