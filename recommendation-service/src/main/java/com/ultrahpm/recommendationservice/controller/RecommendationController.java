package com.ultrahpm.recommendationservice.controller;

import dev.openfeature.sdk.Client;
import com.ultrahpm.recommendationservice.engine.OnnxInferenceEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * REST API for the Recommendation Engine.
 *
 * Exposes the ONNX-powered Neural Collaborative Filtering (NCF) model
 * as a reactive HTTP endpoint that the API Gateway can route to.
 */
@RestController
@RequestMapping("/api/v1/recommendations")
public class RecommendationController {

    private static final Logger log = LoggerFactory.getLogger(RecommendationController.class);

    private final OnnxInferenceEngine onnxEngine;
    private final Client openFeatureClient;

    public RecommendationController(OnnxInferenceEngine onnxEngine, Client openFeatureClient) {
        this.onnxEngine = onnxEngine;
        this.openFeatureClient = openFeatureClient;
    }

    /**
     * Get personalized product recommendations for a user.
     *
     * @param userId the user to generate recommendations for
     * @return a reactive Mono containing the list of recommended product IDs
     */
    @GetMapping("/{userId}")
    public Mono<Map<String, Object>> getRecommendations(@PathVariable Long userId) {
        log.info("Generating recommendations for userId={}", userId);

        boolean useAi = openFeatureClient.getBooleanValue("ai-recommendations-enabled", true);

        if (!useAi) {
            log.info("AI Recommendations disabled via Feature Flag. Returning fallback.");
            return Mono.just(Map.of(
                    "userId", userId,
                    "recommendedProductIds", List.of(1L, 2L, 3L),
                    "engine", "FALLBACK-TOP-SELLERS",
                    "count", 3
            ));
        }

        return onnxEngine.predict(userId, Collections.emptyList())
                .map(productIds -> Map.<String, Object>of(
                        "userId", userId,
                        "recommendedProductIds", productIds,
                        "engine", "ONNX-NCF",
                        "count", productIds.size()
                ))
                .doOnSuccess(result -> log.info("Recommendations generated for userId={}: {} items",
                        userId, result.get("count")));
    }

    /**
     * Health check for the recommendation engine.
     */
    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "UP", "engine", "ONNX Runtime");
    }
}
