# UltraHPM - Getting Started

This guide will help you set up the UltraHPM platform on your local machine for development.

## Prerequisites

Ensure you have the following installed on your system:
- **Java 21 JDK** (Ensure `JAVA_HOME` is set).
- **Maven** (3.8+).
- **Python 3.12+** (For the MLOps pipeline).
- **Docker & Docker Compose** (Ensure Docker Engine is running).
- **Git**.
- **NVIDIA GPU drivers & CUDA Toolkit** (Optional, but highly recommended for fast model training).

## Step 1: Spin up Infrastructure
UltraHPM relies heavily on containerized backing services. Navigate to the infrastructure folder and start the Docker Compose stack.

```bash
cd infrastructure/docker
docker-compose up -d
```
*Note: This will start PostgreSQL, Redis, Elasticsearch, Kafka (in KRaft mode), and Keycloak.*

## Step 2: Build the Java Project
Because we use gRPC for inter-service communication, you must compile the project to allow the `protobuf-maven-plugin` to generate the necessary Java gRPC stubs.

```bash
# In the root 'UltraHPM' directory
mvn clean compile
```

## Step 3: Run the Config Server & Service Registry
All microservices depend on the Spring Cloud Config Server to boot successfully. 

1. Start **Config Server** (port 8888).
2. Start **Service Discovery (Eureka)** (port 8761). It must start immediately after the Config Server so microservices can register upon booting.

## Step 4: Run Core Microservices
Once Config Server and Eureka are up, start the remaining services:
- `api-gateway`
- `product-service`
- `order-service`
- `payment-service`
- `user-service`
- `notification-service`
- `recommendation-service`

Ensure that you have set any necessary environment variables for your local database credentials if they differ from the defaults provided in the config repo.

## Step 5: Setup the ML Pipeline (Python)
If you are working on the recommendation engine, you need to set up the Python ML pipeline.

```bash
cd ml-pipeline
# Create a virtual environment
python -m venv venv

# Activate the virtual environment
# Windows:
.\venv\Scripts\activate
# Linux/Mac:
source venv/bin/activate

# Install heavy dependencies (PyTorch, MLflow, ONNX)
pip install -r requirements.txt
```

To run a training job locally:
```bash
python train.py --epochs 3 --batch_size 1024
```
This will train the NCF model, log metrics to local MLflow (`mlruns/`), and export the `recommendation_model.onnx` file to the `models/` directory for the Java service to pick up.

---
**Next Step**: Read [04_Microservices.md](04_Microservices.md) for coding standards and service-specific deep dives.

## Step 6: GitOps Deployment (ArgoCD)
If you want to test the full production deployment lifecycle locally, you can deploy the stack to a local Kubernetes cluster using ArgoCD.

1. Ensure you have a local Kubernetes cluster running (e.g. \minikube start\ or Docker Desktop Kubernetes).
2. Install ArgoCD into the cluster:
   \\\ash
   kubectl create namespace argocd
   kubectl apply -n argocd -f https://raw.githubusercontent.com/argoproj/argo-cd/stable/manifests/install.yaml
   \\\
3. Apply the root Application definitions to instruct ArgoCD to sync this repository:
   \\\ash
   kubectl apply -f k8s/argocd/argocd-infrastructure.yaml
   kubectl apply -f k8s/argocd/argocd-microservices.yaml
   \\\
4. Forward the ArgoCD UI port to access the dashboard:
   \\\ash
   kubectl port-forward svc/argocd-server -n argocd 8080:443
   \\\
5. Retrieve the initial admin password:
   \\\ash
   kubectl -n argocd get secret argocd-initial-admin-secret -o jsonpath='{.data.password}'
   \\\
*(Note: Base64 decode the password if running on Linux/macOS, or use the UI).*

You will now see ArgoCD automatically spin up the entire UltraHPM stack inside your cluster!

