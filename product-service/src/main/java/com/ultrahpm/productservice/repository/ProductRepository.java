package com.ultrahpm.productservice.repository;

import com.ultrahpm.productservice.domain.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, String> {
    Page<Product> findByCategoryAndActiveTrue(String category, Pageable pageable);
    Page<Product> findByActiveTrue(Pageable pageable);
    Optional<Product> findByIdAndActiveTrue(String id);
    List<Product> findByIdInAndActiveTrue(List<String> ids);
}
