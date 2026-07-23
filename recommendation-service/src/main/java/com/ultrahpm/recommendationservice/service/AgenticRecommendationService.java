package com.ultrahpm.recommendationservice.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import java.util.List;

@Service
public class AgenticRecommendationService {

    private static final Logger log = LoggerFactory.getLogger(AgenticRecommendationService.class);
    private final ChatClient chatClient;

    public AgenticRecommendationService(ChatClient.Builder chatClientBuilder) {
        // Initialize the ChatClient for Agentic workflows (RAG, Tool Calling)
        this.chatClient = chatClientBuilder
            .defaultSystem("You are an elite e-commerce recommendation agent. You can check inventory, apply discounts, and generate highly personalized product recommendations.")
            .build();
    }

    /**
     * Demonstrates an Agentic Workflow where the LLM can decide what to do
     * (e.g. call a tool to check inventory) before recommending.
     */
    public Mono<String> getAgenticRecommendation(String userId, String userContext) {
        log.info("Initiating agentic workflow for user: {}", userId);

        // In 2027 architectures (Java 25+), this would likely be purely synchronous using 
        // Structured Concurrency, but for now we bridge it to our reactive pipeline.
        return Mono.fromCallable(() -> 
            chatClient.prompt()
                .user(userContext)
                // .functions("checkInventory", "applyDiscount") // Example of function calling
                .call()
                .content()
        );
    }
}
