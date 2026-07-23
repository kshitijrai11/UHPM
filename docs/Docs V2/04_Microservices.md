# UltraHPM - Microservices Development Guide

When contributing to UltraHPM's Java microservices, please adhere to the following architectural standards.

## 1. Database-Per-Service
Never share databases between microservices. If the `order-service` needs product data, it must NOT query the `ultrahpm_product` database directly. Instead, it must communicate with the `product-service` via gRPC or REST.
- **For Reactive Services** (`product-service`, `recommendation-service`): Use **R2DBC** for fully non-blocking database pipelines.
- **For Loom/Virtual Thread Services** (`order-service`, `payment-service`): Use standard **Spring Data JPA / JDBC**. Virtual threads make blocking I/O computationally cheap, allowing us to leverage the rich feature set of Hibernate without thread exhaustion.
- **Database Migrations**: Relying on Hibernate's `ddl-auto` or manual SQL scripts is strictly prohibited. You must use **Flyway** for all database schema migrations to version and manage your PostgreSQL clusters securely.

## 2. Inter-Service Communication (gRPC)
For low-latency, synchronous communication between backend services, we use **gRPC**.
- Protobuf definitions (`.proto` files) should be placed in a shared library or within the specific service generating the stubs.
- Always run `mvn clean compile` to generate the latest Java classes from your `.proto` files.
- Use asynchronous gRPC stubs when calling from WebFlux reactive chains to avoid blocking the Netty event loop.

## 3. Kafka & Backpressure
We use **Reactor Kafka** for consuming streams asynchronously, paired with a resilient **3-Layer Backpressure Strategy**. All Kafka producers and consumers must serialize and deserialize payloads using **Avro** schemas. These schemas are validated against the **Confluent Schema Registry** to enforce strict contract evolution and prevent data corruption in the streams.
- **Debezium CDC & Transactional Outbox**: To guarantee data consistency between local databases and Kafka (without distributed locks), we utilize **Debezium Change Data Capture (CDC)**. It tails the PostgreSQL transaction logs (WAL) and automatically publishes any committed Outbox table rows to Kafka.
- **Layer 1 (Bounded Prefetch)**: The Reactor Kafka consumer is configured with a bounded prefetch to prevent OutOfMemory errors during sudden traffic spikes.
- **Layer 2 (Redis Overflow Buffer)**: If the consumer cannot keep up with the prefetch bounds, overflow events are temporarily buffered in a Redis queue to prevent service degradation.
- **Layer 3 (Consumer Group Scaling)**: If the Redis buffer crosses a critical threshold, the orchestrator scales out the consumer group by spinning up additional microservice instances to drain the backlog.
- **Blocking Offloads**: If a Kafka consumer needs to perform a blocking operation (like a heavy synchronous API call), it MUST be offloaded to a bounded elastic scheduler: `.publishOn(Schedulers.boundedElastic())`.

## 4. Concurrency Model Guidelines (Loom vs. WebFlux)
We employ a hybrid concurrency model. Ensure you use the correct paradigm for the service you are building:
- **WebFlux (Reactive)**: Used by `recommendation-service`, `product-service`, and `api-gateway`. These require extreme throughput and non-blocking I/O.
- **Java 21 Virtual Threads (Loom)**: Used by `order-service`, `payment-service`, and `notification-service` where blocking logic (e.g., legacy JPA/Hibernate, synchronous transactional boundaries) is unavoidable. Enable via `spring.threads.virtual.enabled: true`.
- **GraalVM Native Images**: Critical components like the `order-service` APIs are compiled Ahead-of-Time (AOT) using **GraalVM** to achieve instant startup times and drastically lower memory footprints in Kubernetes.
- Do not pool virtual threads. They are cheap to create and should be instantiated per request.
- Be aware of "pinning". Avoid using `synchronized` blocks around blocking I/O; use `ReentrantLock` instead.

## 5. Security Context
- External traffic enters through the API Gateway, where Keycloak validates the JWT.
- The Gateway forwards requests to downstream services. Downstream services should trust the Gateway and can extract the user context (Roles, User ID) from the headers or the forwarded JWT claims.

## 6. ONNX Runtime in Java
When serving ML models in the Recommendation Service:
- ONNX Runtime for Java uses C++ via JNI. Memory allocated for Tensors and Environments is **off-heap**.
- **CRITICAL**: You MUST use Java's `try-with-resources` block when creating `OrtEnvironment`, `OrtSession`, and `OnnxTensor`. Failure to do so will result in massive off-heap native memory leaks that the Java Garbage Collector cannot clean up, ultimately crashing the container.

```java
// Correct Usage Example
try (OrtEnvironment env = OrtEnvironment.getEnvironment();
     OrtSession.SessionOptions options = new OrtSession.SessionOptions();
     OrtSession session = env.createSession(modelPath, options)) {
    
    // Create tensors and run inference inside the try block
    try (OnnxTensor inputTensor = OnnxTensor.createTensor(env, inputData)) {
        // ...
    }
}
```

## 7. Resilience & Fault Tolerance (Resilience4j)
In a distributed architecture, partial failures are guaranteed. All inter-service synchronous communication (gRPC/REST) must be wrapped in fault-tolerance mechanisms using Resilience4j.

- **Circuit Breakers**: Must be configured on all remote calls. If a downstream service's failure rate exceeds a threshold (e.g., 50% over a sliding window), the circuit opens, and requests immediately fail-fast or route to a fallback method.
- **Retries**: Use for transient network errors. Configure a maximum of 3 retries with an exponential backoff strategy. Do not retry non-idempotent operations (like non-idempotent POST/Payment requests) unless explicitly safe.
- **Timeouts**: No infinite waiting. Every gRPC stub and WebClient call must have a strict timeout (e.g., 500ms - 2s depending on the SLA).
- **Fallbacks**: Where applicable, provide sensible fallbacks. For example, if the recommendation-service fails, the API should catch the exception and return a default list of "Trending Products" rather than throwing a 500 Error to the user.

## 8. Distributed Transactions (SAGA Pattern)
The `order-service` orchestrates complex, distributed transactions that span multiple independent domains (e.g., `payment-service` and `product-service`) using the **SAGA Pattern**. This approach ensures eventual consistency through local transactions and compensatory actions, avoiding the performance bottlenecks of traditional two-phase commits (2PC) or distributed locks.

---
**Next Step**: Read [05_MLOps_Pipeline.md](05_MLOps_Pipeline.md) to understand the model training and deployment lifecycle.

## 5. Agentic Workflows & Native JVM AI (Spring AI)
We are actively building Agentic Workflows into our microservices using **Spring AI**.
- **Local Embeddings**: The \product-service\ uses \spring-ai-transformers\ to embed queries locally using ONNX, eliminating network latency to OpenAI.
- **Vector Storage**: We rely on **pgvector** in PostgreSQL to store and search high-dimensional embeddings natively.
- **Local RAG**: Retrieval-Augmented Generation is handled by fetching similar products from \pgvector\ and feeding them as context into a local **Ollama** LLM container for cost-free, private text generation.

