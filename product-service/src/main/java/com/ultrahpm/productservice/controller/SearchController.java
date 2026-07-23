package com.ultrahpm.productservice.controller;

import com.ultrahpm.productservice.search.ProductDocument;
import com.ultrahpm.productservice.search.ProductSearchRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/products/search")
public class SearchController {

    private final ProductSearchRepository searchRepository;

    public SearchController(ProductSearchRepository searchRepository) {
        this.searchRepository = searchRepository;
    }

    @GetMapping
    public Page<ProductDocument> search(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return searchRepository.findByNameOrDescription(query, query, PageRequest.of(page, size));
    }
}
