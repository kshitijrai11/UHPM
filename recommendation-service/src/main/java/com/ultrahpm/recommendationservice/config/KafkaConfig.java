package com.ultrahpm.recommendationservice.config;

import com.ultrahpm.recommendationservice.event.UserEvent;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverOptions;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers:localhost:9092}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id:recommendation-group}")
    private String groupId;

    @Bean
    public KafkaReceiver<String, UserEvent> kafkaReceiver() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");

        // Backpressure layer 1: Bounded prefetch
        ReceiverOptions<String, UserEvent> receiverOptions = ReceiverOptions.<String, UserEvent>create(props)
                .subscription(Collections.singleton("user-events"))
                .commitBatchSize(100)
                .commitInterval(java.time.Duration.ofMillis(1000));

        return KafkaReceiver.create(receiverOptions);
    }
}
