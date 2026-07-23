# UltraHPM - Observability Stack

In an asynchronous, highly concurrent, and distributed system like UltraHPM, standard logging is insufficient. We rely on a comprehensive observability stack to guarantee tracing, logging, and metrics across all microservices, Kafka streams, and gRPC calls.

## 1. Distributed Tracing (Jaeger / Tempo & OpenTelemetry)
Every request entering the **API Gateway** is assigned a unique `traceId`.
- **OpenTelemetry** agents instrument our Java services to propagate this `traceId` automatically.
- **Context Propagation**: The `traceId` is propagated across HTTP boundaries, **gRPC metadata**, and **Kafka record headers**. This allows us to trace a user's action from the initial API Gateway request, through a Kafka topic, and into the downstream consumer (e.g., Notification Service or Recommendation Service).
- **Backend**: We use Jaeger (or Grafana Tempo) to visualize the distributed traces and identify latency bottlenecks.

## 2. Log Aggregation (Loki)
We use **Grafana Loki** for log aggregation.
- Microservices stream logs to Loki using Promtail or Docker log drivers.
- Logs are automatically indexed by labels (e.g., `service_name`, `traceId`, `environment`).
- **Correlation**: Because OpenTelemetry injects the `traceId` into our SLF4J/Logback MDC (Mapped Diagnostic Context), you can query Loki for a specific `traceId` and see all logs from all microservices involved in that transaction.

## 3. Metrics (Prometheus & Grafana)
**Prometheus** scrapes metrics from the `/actuator/prometheus` endpoint exposed by each Spring Boot microservice.
- **System Metrics**: CPU, memory, JVM garbage collection, and Virtual Thread counts.
- **Business Metrics**: Orders placed, payments processed, and Kafka consumer lag.
- **MLOps Metrics**: ONNX inference latency, recommendation cache hit rates, and A/B test conversion rates.
- **Dashboards**: **Grafana** acts as the single pane of glass, visualizing Prometheus metrics and Loki logs side-by-side.

## 4. Best Practices for Developers
1. **Never Log Sensitive Data**: Ensure PII (Personally Identifiable Information) and JWT tokens are masked or omitted from logs.
2. **Use SLF4J**: Always use the SLF4J API (`log.info()`). Do not use `System.out.println()`.
3. **Custom Metrics**: Use the Micrometer `MeterRegistry` injected into your Spring components to create custom counters and timers for critical business flows.
4. **Reactive MDC**: When using WebFlux, ensure that the MDC context is correctly propagated across reactive thread boundaries using `ContextSnapshot` (Micrometer Context Propagation library).
