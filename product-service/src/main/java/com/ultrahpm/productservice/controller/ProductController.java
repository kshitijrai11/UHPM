package com.ultrahpm.productservice.controller;

import com.ultrahpm.productservice.dto.CreateProductRequest;
import com.ultrahpm.productservice.dto.PagedResponse;
import com.ultrahpm.productservice.dto.ProductDTO;
import com.ultrahpm.productservice.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public PagedResponse<ProductDTO> getProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String category) {
        return productService.getProducts(page, size, category);
    }

    @GetMapping("/{id}")
    public ProductDTO getProductById(@PathVariable String id) {
        return productService.getProductById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductDTO createProduct(@Valid @RequestBody CreateProductRequest request) {
        return productService.createProduct(request);
    }
}
