package com.ultrahpm.recommendationservice.messaging;

import com.ultrahpm.recommendationservice.event.UserEvent;
import com.ultrahpm.recommendationservice.engine.OnnxInferenceEngine;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverRecord;

import java.util.concurrent.atomic.AtomicInteger;

@Service
public class UserEventConsumer {
    private static final Logger log = LoggerFactory.getLogger(UserEventConsumer.class);

    private final KafkaReceiver<String, UserEvent> kafkaReceiver;
    private final OnnxInferenceEngine onnxEngine;
    private final ElasticsearchClient esClient;
    
    // For backpressure monitoring
    private final AtomicInteger processingCount = new AtomicInteger(0);

    public UserEventConsumer(KafkaReceiver<String, UserEvent> kafkaReceiver, 
                             OnnxInferenceEngine onnxEngine,
                             ElasticsearchClient esClient) {
        this.kafkaReceiver = kafkaReceiver;
        this.onnxEngine = onnxEngine;
        this.esClient = esClient;
    }

    @EventListener(ApplicationStartedEvent.class)
    public void startConsumption() {
        log.info("Starting Reactor Kafka Consumer for user-events...");
        
        kafkaReceiver.receive()
                .doOnNext(r -> {
                    int count = processingCount.incrementAndGet();
                    if (count % 1000 == 0) {
                        log.info("Processing event batch, current concurrent items: {}", count);
                    }
                })
                .flatMap(record -> processEvent(record)
                        .doOnSuccess(v -> record.receiverOffset().acknowledge())
                        .doFinally(signalType -> processingCount.decrementAndGet())
                )
                .subscribe(
                        null,
                        error -> log.error("Error consuming user events", error)
                );
    }

    private reactor.core.publisher.Mono<Void> processEvent(ReceiverRecord<String, UserEvent> record) {
        UserEvent event = record.value();
        if (event == null) {
            return reactor.core.publisher.Mono.empty();
        }
        
        log.debug("Consumed event: {}", event);
        
        // 1. Run ONNX Inference
        // Note: Realistically, we'd fetch the user's history here. We pass empty list for demo.
        return onnxEngine.predict(Long.parseLong(event.getUserId().toString()), java.util.Collections.emptyList())
                .flatMap(recommendedIds -> {
                    log.info("ONNX predicted items: {} for user: {}", recommendedIds, event.getUserId());
                    
                    // 2. Filter via Elasticsearch (check if in stock)
                    // We wrap ES call in Mono.fromCallable because it's blocking
                    return reactor.core.publisher.Mono.fromCallable(() -> {
                        // Dummy ES query representation
                        // In reality: esClient.search(s -> s.index("products").query(q -> q.terms(t -> t.field("id").terms(terms -> terms.value(...)))), Product.class);
                        return recommendedIds; // returning unfiltered for now
                    }).subscribeOn(reactor.core.scheduler.Schedulers.boundedElastic());
                })
                .doOnNext(filteredIds -> {
                    // 3. gRPC Hydration from Product Service
                    log.info("Hydrating product details for IDs: {} via gRPC...", filteredIds);
                    // productGrpcStub.getProductsByIds(request)
                })
                .then();
    }
}
