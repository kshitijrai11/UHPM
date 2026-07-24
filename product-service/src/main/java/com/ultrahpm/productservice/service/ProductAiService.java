package com.ultrahpm.productservice.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import com.ultrahpm.productservice.domain.Product;
import com.ultrahpm.productservice.repository.ProductRepository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ProductAiService {

    private final VectorStore vectorStore;
    private final ChatClient chatClient;
    private final ProductRepository productRepository;

    public ProductAiService(VectorStore vectorStore, ChatClient.Builder chatClientBuilder, ProductRepository productRepository) {
        this.vectorStore = vectorStore;
        this.chatClient = chatClientBuilder.build();
        this.productRepository = productRepository;
    }

    /**
     * Seeds the Vector Store with our products for the RAG pipeline.
     * In a real application, this would be an event-driven sync (e.g. Debezium CDC -> Kafka -> VectorStore).
     */
    public void syncProductsToVectorStore() {
        List<Product> products = productRepository.findAll();
        List<Document> documents = products.stream()
                .map(p -> new Document(
                        p.getId().toString(),
                        String.format("Product: %s. Description: %s. Category: %s. Price: $%.2f",
                                p.getName(), p.getDescription(), p.getCategory(), p.getPrice()),
                        Map.<String, Object>of(
                                "name", p.getName(),
                                "category", p.getCategory(),
                                "price", p.getPrice()
                        )
                ))
                .collect(Collectors.toList());

        vectorStore.accept(documents);
    }

    /**
     * Retrieval-Augmented Generation (RAG) implementation.
     * 1. Embeds the user query and finds similar products in pgvector.
     * 2. Feeds the results to Ollama (LLM) to generate a helpful response.
     */
    public String askAssistant(String question) {
        // Step 1: Retrieval (Similarity Search using local ONNX embeddings via pgvector)
        List<Document> similarProducts = vectorStore.similaritySearch(
                SearchRequest.query(question).withTopK(3)
        );

        String context = similarProducts.stream()
                .map(Document::getContent)
                .collect(Collectors.joining("\n\n"));

        // Step 2: Generation (LLM prompt with context via Ollama)
        String prompt = String.format("""
                You are a helpful retail assistant. Use the following product information to answer the user's question.
                If you don't know the answer or the products don't match, say you cannot help. Do not make up products.
                
                Product Context:
                %s
                
                User Question:
                %s
                """, context, question);

        return chatClient.prompt()
                .user(prompt)
                .call()
                .content();
    }
}
