# Ultra High-Performance Spring Boot Microservices Platform

## Overview
This project is a **modular, ultra-scalable, low-latency microservices platform** built using **Spring Boot 3** and **Java 21**. It incorporates a modern reactive stack and cloud-native best practices to deliver a resilient, scalable, and observable system suitable for local development, testing, and production deployments on Kubernetes and Docker.

---

## Table of Contents
- [Features](#features)
- [Architecture](#architecture)
- [Technologies](#technologies)
- [Getting Started](#getting-started)
    - [Prerequisites](#prerequisites)
    - [Running Locally](#running-locally)
    - [Production Deployment](#production-deployment)
- [Development Workflow](#development-workflow)
- [Observability & Monitoring](#observability--monitoring)
- [Security](#security)
- [Project Structure](#project-structure)
- [Contributing](#contributing)
- [License](#license)

---

## Features
- Highly reactive, non-blocking microservices using **Spring WebFlux**, **gRPC**, and **GraphQL**
- Centralized entry point with **Spring Cloud Gateway** API Gateway supporting routing, rate limiting, and security
- Asynchronous event-driven communication via **Apache Kafka** with robust schema management (Avro + Schema Registry)
- Dynamic service discovery with **Eureka**
- Externalized, version-controlled configuration via **Spring Cloud Config** backed by Git
- Fault tolerance using **Resilience4j** circuit breakers, retries, and bulkheads
- End-to-end observability with **Prometheus**, **Grafana**, **Jaeger**, **Tempo**, **Loki**, and **OpenTelemetry**
- Enterprise-grade security using **OAuth2/OIDC** with **Keycloak** and mutual TLS (mTLS)
- Reactive data access through **PostgreSQL** using **R2DBC** and caching via **Redis**
- Containerized environment using **Docker Compose** for local setup and **Kubernetes** manifests for production
- Support for dynamic autoscaling (Horizontal and Vertical Pod Autoscalers) and graceful failover

---

## Architecture
The platform consists of multiple independently deployable microservices:

- **API Gateway:** Central routing and security enforcement with Spring Cloud Gateway
- **Core Services:** User, Order, Payment, Notification — each with its own database, cache, and Kafka messaging
- **Asynchronous Messaging:** Kafka handles event sourcing, decoupling, and auditability
- **Service Registry:** Eureka enables service discovery and load balancing
- **Config Server:** Centralized dynamic configuration refresh
- **Observability Stack:** Metrics, logging, tracing, and visualization integrated via a comprehensive toolchain
- **Security:** Keycloak provides OAuth2 and OpenID Connect for authentication and authorization, with network security via mTLS

---

## Technologies
| Layer         | Technologies & Tools                               |
|---------------|--------------------------------------------------|
| Language      | Java 21, Virtual Threads                          |
| Frameworks    | Spring Boot 3, Spring WebFlux, Spring Cloud Gateway, GraphQL, gRPC |
| Messaging     | Apache Kafka, Kafka Schema Registry (Avro)       |
| Resilience    | Resilience4j (Circuit Breakers, Retry, Bulkhead) |
| Data          | PostgreSQL with R2DBC (Reactive), Redis          |
| Security      | Keycloak (OAuth2/OIDC), mTLS                      |
| Containerization | Docker, Docker Compose, Kubernetes                |
| Observability | Prometheus, Grafana, Jaeger, Tempo, Loki, OpenTelemetry |
| Config        | Spring Cloud Config (Git-backed)                  |

---

## Getting Started

### Prerequisites
- [Java 21 JDK](https://jdk.java.net/21/)
- [Docker and Docker Compose](https://www.docker.com/products/docker-desktop)
- [Kubernetes cluster](https://kubernetes.io/docs/setup/) (for production)
- [Keycloak](https://www.keycloak.org/) server
- PostgreSQL and Redis instances or containers

### Running Locally
1. Build and start infrastructure using Docker Compose:
    ```
    docker-compose up --build
    ```
   This will start Kafka (in KRaft mode), Schema Registry, Redis, PostgreSQL, Eureka, Prometheus, Grafana, Jaeger, and Keycloak.

2. Scaffold and run individual microservices locally with the appropriate dependencies.

3. Access APIs via the API Gateway endpoint:
    - GraphQL, REST, and gRPC endpoints available

### Production Deployment
1. Deploy microservices and infrastructure using Kubernetes manifests (`k8s.yaml` and related files).
2. Set resource limits, readiness, and liveness probes.
3. Enable Horizontal and Vertical Pod Autoscalers.
4. Configure Prometheus scraping annotations for monitoring.
5. Manage configuration dynamically via Git-based Spring Cloud Config.
6. Harden security with Keycloak and secure network policies.

---

## Development Workflow
- Use Avro and Proto schema contracts for Kafka topics and gRPC/GraphQL APIs.
- Implement reactive endpoints using Spring WebFlux and gRPC.
- Apply resilience patterns (retry, circuit breaker, rate limiting).
- Instrument metrics via Micrometer and distributed tracing through OpenTelemetry.
- Load test with K6, JMeter, or Gatling to ensure performance under concurrency.
- Use Grafana and Jaeger dashboards for monitoring and diagnostics.

---

## Observability & Monitoring
- **Metrics:** Micrometer exports to Prometheus
- **Visualization:** Grafana dashboards pre-configured for service health and performance
- **Tracing:** Distributed tracing with Jaeger and Tempo for request flow analysis
- **Logging:** Loki centralizes application logs with query and correlation support
- **Instrumentation:** OpenTelemetry SDK added to all services for uniform telemetry data

---

## Security
- OAuth2 and OpenID Connect authentication via Keycloak
- API Gateway enforces token validation and applies security policies
- Mutual TLS (mTLS) for service-to-service encryption and zero trust networking
- Role-based access control (RBAC) supported across services

---

## Project Structure
├── api-gateway/ # Spring Cloud Gateway service
├── user-service/ # User management microservice
├── order-service/ # Order processing microservice
├── payment-service/ # Payment transactions microservice
├── notification-service/ # Notification and messaging microservice
├── shared/ # Common utilities, Kafka schema definitions
├── infra/
│ ├── docker-compose.yml # Local infrastructure setup
│ └── k8s/ # Kubernetes manifests and Helm charts
└── config/ # Spring Cloud Config repository (Git)

---

## Contributing
Contributions are welcome! Please follow these steps:

1. Fork the repository.
2. Create your feature branch (`git checkout -b feature-name`).
3. Commit your changes (`git commit -m 'Add feature'`).
4. Push to your branch (`git push origin feature-name`).
5. Open a Pull Request describing your changes.

For major changes, please open an issue first to discuss your plans.


