# UltraHPM - Architecture

UltraHPM leverages a modern, distributed architecture designed for maximum throughput and minimal latency.

## High-Level Components

### 1. Edge Layer
- **API Gateway**: Built on Spring Cloud Gateway. Acts as the single entry point. Exposes both REST and **GraphQL** endpoints for frontend web/mobile clients. Handles JWT token validation via Keycloak, distributed rate limiting (Redis-backed Token Bucket algorithm to enforce per-user/IP quotas), and routes requests to downstream services. Supports weighted routing for A/B testing ML models (e.g., 80% to v1, 20% to v2).

### 2. Infrastructure Layer
- **Service Discovery (Eureka)**: Netflix Eureka for dynamic service registration and discovery.
- **Spring Cloud Config Server**: Centralized configuration management. All microservices pull their configuration (`bootstrap.yml`) from this server on startup.
- **Keycloak**: Identity and Access Management (IAM) for issuing JWT tokens.
- **Apache Kafka**: The central nervous system for asynchronous event streaming (e.g., `user-events` topic). Leverages the **Confluent Schema Registry** with **Avro** schemas (e.g., `user-events.avsc`, `order-events.avsc`) to serialize payloads and enforce strict contract evolution.
- **Elasticsearch**: Used for fast, cross-referenced product availability and full-text search.
- **PostgreSQL**: The primary relational datastore. Each service gets its own isolated database instance/schema.

### 3. Core Microservices
UltraHPM is composed of several independent domains:
- **Product Service**: Manages catalog, inventory, and product details.
- **Order Service**: Handles order lifecycle, cart conversion, and orchestrates distributed transactions.
- **Payment Service**: Processes transactions and payment gateways. Operates within a strictly isolated network boundary to ensure **PCI-DSS Compliance**, ensuring sensitive cardholder data never leaks to other domains.
- **User Service**: Manages user profiles, preferences, and addresses.
- **Notification Service**: Leverages the **Kafka Streams API** for stateful stream processing. Instead of processing single events sequentially, it can aggregate time-windows of events (e.g., aggregating order updates into a single daily digest) for asynchronous notifications (email, SMS, push).
- **Recommendation Service**: The ML-powered brain. Consumes user events from Kafka, generates personalized recommendations using an embedded ONNX model, and communicates with the Product Service via **gRPC** for rapid hydration of product data.

## Security Context
- **Zero-Trust Security**: The platform adopts a true Zero-Trust model. Edge security is enforced via Keycloak OAuth2 / JWT Validation. Internal service-to-service communication (especially gRPC calls) is strictly encrypted and mutually authenticated using **mTLS (Mutual TLS)**.

## Communication Patterns

1. **Synchronous (Service-to-Service)**: **gRPC** is the standard for inter-service RPC calls (e.g., Recommendation Service fetching product details from Product Service). It uses HTTP/2 and Protobufs for extreme speed and low serialization overhead.
2. **Asynchronous (Event-Driven)**: **Apache Kafka** is used for decoupled events. For example, when a user clicks a product, an event is fired into Kafka. The Recommendation service consumes this via Reactor Kafka.

## Concurrency Model
- **Reactive (WebFlux)**: Used for high-concurrency, non-blocking I/O paths (e.g., API Gateway, Kafka ingestion, database calls via R2DBC).
- **Virtual Threads (Java 21)**: Used where blocking is unavoidable (e.g., interacting with legacy libraries, certain complex business logic that benefits from an imperative programming style) avoiding platform thread exhaustion.

## MLOps Architecture
- **Offline Training**: Python + PyTorch running on GPU. Trains Neural Collaborative Filtering (NCF) models on historical PostgreSQL interaction data.
- **Tracking**: MLflow (SQLite backend) for experiment tracking and metric logging.
- **Model Serialization**: PyTorch models are exported to statically-sized **ONNX** formats.
- **Online Inference**: Java microservice loads `.onnx` files using ONNX Runtime for Java. Native memory is strictly managed using `try-with-resources` to prevent off-heap leaks.

---
**Next Step**: Read [06_Observability.md](06_Observability.md) to learn about our tracing and logging standards.
