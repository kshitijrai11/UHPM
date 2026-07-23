package com.ultrahpm.productservice.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProductDTO(
        String id,
        String name,
        String category,
        BigDecimal price,
        Integer stockQuantity,
        String description,
        String imageUrl,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
