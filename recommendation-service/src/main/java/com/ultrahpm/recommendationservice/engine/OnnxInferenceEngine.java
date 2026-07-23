package com.ultrahpm.recommendationservice.engine;

import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;
import ai.onnxruntime.OrtSession.Result;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class OnnxInferenceEngine {

    private static final Logger log = LoggerFactory.getLogger(OnnxInferenceEngine.class);

    @Value("${ml.model.path:/models/recommendation_model.onnx}")
    private String modelPath;

    private OrtEnvironment env;
    private OrtSession session;

    // Use an executor with virtual threads for the blocking JNI calls
    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

    @PostConstruct
    public void init() {
        try {
            log.info("Loading ONNX model from: {}", modelPath);
            env = OrtEnvironment.getEnvironment();
            OrtSession.SessionOptions options = new OrtSession.SessionOptions();
            options.setOptimizationLevel(OrtSession.SessionOptions.OptLevel.ALL_OPT);
            session = env.createSession(modelPath, options);
            log.info("ONNX model loaded successfully.");
        } catch (OrtException e) {
            log.error("Failed to initialize ONNX Runtime: {}", e.getMessage());
            // In a real app we might not crash on startup if fallback is possible, 
            // but for UltraHPM the recommendation engine is useless without it.
        }
    }

    @PreDestroy
    public void cleanup() {
        try {
            if (session != null) {
                session.close();
            }
            if (env != null) {
                env.close();
            }
            log.info("ONNX resources cleaned up.");
        } catch (OrtException e) {
            log.error("Error closing ONNX resources", e);
        }
    }

    /**
     * Runs the ONNX model inference using Virtual Threads to offload the blocking JNI call.
     */
    public Mono<List<Long>> predict(Long userId, List<Long> userHistory) {
        return Mono.fromCallable(() -> {
            // This runs on a virtual thread
            List<Long> recommendedItemIds = new ArrayList<>();
            
            // Dummy logic to create inputs based on NCF expectations (user, item)
            // For real NCF, we'd iterate over candidate items. 
            // For demonstration, let's assume a simplified batch input tensor.
            
            long[] users = new long[]{userId, userId, userId, userId, userId};
            long[] items = new long[]{101L, 102L, 103L, 104L, 105L}; // Candidates
            
            // The critical try-with-resources block for off-heap native memory
            try (OnnxTensor userTensor = OnnxTensor.createTensor(env, LongBuffer.wrap(users), new long[]{5});
                 OnnxTensor itemTensor = OnnxTensor.createTensor(env, LongBuffer.wrap(items), new long[]{5})) {
                
                Map<String, OnnxTensor> inputs = Map.of(
                        "user", userTensor,
                        "item", itemTensor
                );
                
                try (Result result = session.run(inputs)) {
                    // Assuming the output is a probability score tensor
                    float[][] scores = (float[][]) result.get(0).getValue();
                    
                    // Simple thresholding or top-K logic
                    for (int i = 0; i < scores.length; i++) {
                        if (scores[i][0] > 0.5f) {
                            recommendedItemIds.add(items[i]);
                        }
                    }
                }
            } catch (OrtException e) {
                log.error("ONNX inference failed", e);
            }
            
            // Fallback if empty
            if (recommendedItemIds.isEmpty()) {
                return List.of(101L, 102L, 103L);
            }
            return recommendedItemIds;
        }).subscribeOn(reactor.core.scheduler.Schedulers.fromExecutor(executor));
    }
}
