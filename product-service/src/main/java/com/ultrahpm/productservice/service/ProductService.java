package com.ultrahpm.productservice.service;

import com.ultrahpm.productservice.domain.Product;
import com.ultrahpm.productservice.dto.CreateProductRequest;
import com.ultrahpm.productservice.dto.PagedResponse;
import com.ultrahpm.productservice.dto.ProductDTO;
import com.ultrahpm.productservice.repository.ProductRepository;
import com.ultrahpm.productservice.search.ProductDocument;
import com.ultrahpm.productservice.search.ProductSearchRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ProductService {
    private static final Logger log = LoggerFactory.getLogger(ProductService.class);
    private final ProductRepository productRepository;
    private final ProductSearchRepository searchRepository;
    private final TwoTierCacheService cacheService;

    public ProductService(ProductRepository productRepository, ProductSearchRepository searchRepository, TwoTierCacheService cacheService) {
        this.productRepository = productRepository;
        this.searchRepository = searchRepository;
        this.cacheService = cacheService;
    }

    public ProductDTO getProductById(String id) {
        log.info("Fetching product by id: {} on thread {}", id, Thread.currentThread().getName());
        Product product = cacheService.getProduct(id, () -> productRepository.findByIdAndActiveTrue(id));
        return mapToDTO(product);
    }

    public PagedResponse<ProductDTO> getProducts(int page, int size, String category) {
        log.info("Fetching products page {}, size {}, category {} on thread {}", page, size, category, Thread.currentThread().getName());
        Pageable pageable = PageRequest.of(page, size);
        Page<Product> productPage;

        if (category != null && !category.isBlank()) {
            productPage = productRepository.findByCategoryAndActiveTrue(category, pageable);
        } else {
            productPage = productRepository.findByActiveTrue(pageable);
        }

        Page<ProductDTO> dtoPage = productPage.map(this::mapToDTO);
        return PagedResponse.of(dtoPage);
    }

    public List<ProductDTO> getProductsByIds(List<String> ids) {
        return productRepository.findByIdInAndActiveTrue(ids)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public ProductDTO createProduct(CreateProductRequest request) {
        log.info("Creating new product: {} on thread {}", request.name(), Thread.currentThread().getName());
        Product product = new Product(
                request.name(),
                request.category(),
                request.price(),
                request.stockQuantity(),
                request.description(),
                request.imageUrl()
        );
        product = productRepository.save(product);
        
        // Sync with Elasticsearch
        ProductDocument doc = new ProductDocument(
                product.getId(),
                product.getName(),
                product.getCategory(),
                product.getPrice(),
                product.getDescription(),
                product.isActive()
        );
        searchRepository.save(doc);
        
        // Evict from caches
        cacheService.evict(product.getId());
        
        return mapToDTO(product);
    }

    private ProductDTO mapToDTO(Product product) {
        return new ProductDTO(
                product.getId(),
                product.getName(),
                product.getCategory(),
                product.getPrice(),
                product.getStockQuantity(),
                product.getDescription(),
                product.getImageUrl(),
                product.isActive(),
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }
}
