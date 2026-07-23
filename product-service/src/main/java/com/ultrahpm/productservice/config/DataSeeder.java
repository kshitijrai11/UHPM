package com.ultrahpm.productservice.config;

import com.ultrahpm.productservice.domain.Product;
import com.ultrahpm.productservice.repository.ProductRepository;
import com.ultrahpm.productservice.search.ProductDocument;
import com.ultrahpm.productservice.search.ProductSearchRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
@Profile("!test")
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);
    
    private final ProductRepository productRepository;
    private final ProductSearchRepository searchRepository;

    public DataSeeder(ProductRepository productRepository, ProductSearchRepository searchRepository) {
        this.productRepository = productRepository;
        this.searchRepository = searchRepository;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (productRepository.count() == 0) {
            log.info("Starting database seeding for products...");
            seedProducts();
            log.info("Database seeding completed.");
        }
    }

    private void seedProducts() {
        String[] categories = {"Electronics", "Clothing", "Home", "Sports", "Books"};
        Random random = new Random();
        List<Product> products = new ArrayList<>();
        List<ProductDocument> docs = new ArrayList<>();

        for (int i = 1; i <= 5000; i++) {
            String category = categories[random.nextInt(categories.length)];
            BigDecimal price = BigDecimal.valueOf(10 + (990 * random.nextDouble())).setScale(2, java.math.RoundingMode.HALF_UP);
            int stock = random.nextInt(1000);
            
            Product p = new Product(
                "Product " + i,
                category,
                price,
                stock,
                "This is a high quality " + category.toLowerCase() + " item.",
                "https://example.com/images/prod" + i + ".jpg"
            );
            products.add(p);
            
            // Batch save every 500 to avoid out of memory
            if (i % 500 == 0) {
                List<Product> saved = productRepository.saveAll(products);
                for (Product savedProd : saved) {
                    docs.add(new ProductDocument(
                        savedProd.getId(),
                        savedProd.getName(),
                        savedProd.getCategory(),
                        savedProd.getPrice(),
                        savedProd.getDescription(),
                        savedProd.isActive()
                    ));
                }
                searchRepository.saveAll(docs);
                products.clear();
                docs.clear();
                log.info("Seeded {} products...", i);
            }
        }
    }
}
