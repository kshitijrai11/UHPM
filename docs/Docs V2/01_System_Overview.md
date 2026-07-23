# UltraHPM - System Overview

Welcome to **UltraHPM** (Ultra High-Performance Microservices). This is an e-commerce platform built to demonstrate FANG-level engineering standards, emphasizing extreme performance, massive scalability, and cutting-edge MLOps integration.

## Core Philosophy

1. **Extreme Performance**: Combines Java 21 Virtual Threads with Spring WebFlux/Reactor for a hybrid concurrency model. Blocking operations (like legacy JDBC) are pushed to virtual threads, while the core serving pipeline remains purely reactive via R2DBC.
2. **Event-Driven Resilience**: Uses Apache Kafka with a robust three-layer backpressure strategy to handle massive traffic spikes without service degradation.
3. **Strict Boundaries**: Adheres strictly to the **Database-per-Service** pattern to ensure loose coupling and independent scalability.
4. **Agentic AI & Native ML Serving**: Integrates Spring AI and `pgvector` to run a fully native Retrieval-Augmented Generation (RAG) pipeline in the JVM (using local Ollama models). Additionally, recommendation ML models are executed natively using the ONNX Runtime (C++ JNI) for microsecond-level latency.
5. **Zero-Trust Security**: Secured at the edge using Spring Cloud Gateway acting as an OAuth2 Resource Server backed by Keycloak JWTs.
6. **Autoscaling & Container Orchestration**: Deployed to Kubernetes with fully automated Horizontal Pod Autoscalers (HPA) and Vertical Pod Autoscalers (VPA) that dynamically scale pods based on real-time CPU and Memory metrics.
7. **Bulletproof Testing**: Enforces architectural boundaries via ArchUnit, verifies reactive streams via StepVerifier, relies on disposable Docker infrastructure via Testcontainers (`@ServiceConnection`), and uses Spring Cloud Contract for consumer-driven contract testing.

## Target Audience

This documentation is intended for backend engineers, ML engineers, and platform architects joining the UltraHPM team. It assumes familiarity with:
- Java 21 & Spring Boot 3.x
- Reactive Programming (Project Reactor, WebFlux)
- Microservices Architecture (gRPC, Service Discovery, Config Management)
- Containerization & Infrastructure (Docker, Kafka, PostgreSQL, Elasticsearch, Redis)
- Machine Learning Basics (PyTorch, ONNX, MLflow)

---
**Next Step**: Read [02_Architecture.md](02_Architecture.md) to understand the high-level system design.
