# UltraHPM - V2 Documentation

Welcome to the central documentation hub for **UltraHPM**. This documentation is designed to onboard new developers and provide comprehensive architectural, operational, and development guidelines for the platform.

## Table of Contents

1. **[System Overview](01_System_Overview.md)** 
   - *Start here.* High-level goals, core philosophy, and what makes this an "Ultra High-Performance" system.
2. **[Architecture](02_Architecture.md)**
   - Detailed breakdown of the hybrid concurrency models, inter-service communication (gRPC/Kafka), and database patterns.
3. **[Getting Started](03_Getting_Started.md)**
   - A step-by-step guide to setting up your local environment, spinning up the infrastructure, and running the microservices.
4. **[Microservices Guide](04_Microservices.md)**
   - Coding standards, reactive programming best practices, Virtual Thread usage, and strict boundary rules.
5. **[MLOps & AI Pipeline](05_MLOps_Pipeline.md)**
   - How to work with the PyTorch recommendation models, MLflow experiment tracking, and ONNX Java runtime integration.
6. **[Observability Stack](06_Observability.md)**
   - Distributed tracing (OpenTelemetry/Jaeger), log aggregation (Loki), and metric collection (Prometheus/Grafana).
7. **[Architecture Vision 2027](07_Architecture_Vision_2027.md)**
   - The strategic roadmap preparing UltraHPM for Agentic Workflows, Java 25, and GraphQL Federation.

---

### Core Technologies
- **Java 21** (Virtual Threads)
- **Spring Boot 3.x** (WebFlux, R2DBC)
- **Python 3.12** (PyTorch, MLflow)
- **Apache Kafka** (Reactor Kafka)
- **gRPC / Protobuf**
- **ONNX Runtime**
- **PostgreSQL, Redis, Elasticsearch, Keycloak**
