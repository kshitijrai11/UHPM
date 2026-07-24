package com.ultrahpm.productservice.controller;

import com.ultrahpm.productservice.service.ProductAiService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/products")
public class ProductAssistantController {

    private final ProductAiService productAiService;

    public ProductAssistantController(ProductAiService productAiService) {
        this.productAiService = productAiService;
    }

    @PostMapping("/assistant")
    public ResponseEntity<Map<String, String>> askAssistant(@RequestBody Map<String, String> request) {
        String question = request.get("question");
        if (question == null || question.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Question cannot be empty"));
        }
        
        String answer = productAiService.askAssistant(question);
        return ResponseEntity.ok(Map.of("answer", answer));
    }

    @PostMapping("/assistant/sync")
    public ResponseEntity<Map<String, String>> syncStore() {
        productAiService.syncProductsToVectorStore();
        return ResponseEntity.ok(Map.of("status", "Products synced to vector store successfully."));
    }
}
