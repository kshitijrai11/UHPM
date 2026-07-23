#!/bin/bash

# UltraHPM Kubernetes Deployment Script
# Applies all manifests in the correct order.

set -e

echo "==========================================="
echo " Deploying UltraHPM to Kubernetes          "
echo "==========================================="

# Ensure we are in the project root
cd "$(dirname "$0")/.."

echo "1. Applying Infrastructure Services (Kafka, Postgres, Redis, Elasticsearch)..."
kubectl apply -f k8s/infrastructure/

echo "Waiting for Infrastructure to initialize (30s)..."
sleep 30

echo "2. Applying Microservices..."
kubectl apply -f k8s/microservices/

echo "3. Applying Autoscaling (HPA)..."
kubectl apply -f k8s/autoscaling/

echo "4. Applying API Gateway & Routing..."
kubectl apply -f k8s/routing/

echo "==========================================="
echo " Deployment Triggered!                     "
echo " Monitor status with: kubectl get pods -w  "
echo "==========================================="
