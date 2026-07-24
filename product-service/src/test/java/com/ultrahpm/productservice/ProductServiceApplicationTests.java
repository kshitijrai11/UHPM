package com.ultrahpm.productservice;

import com.ultrahpm.productservice.repository.ProductRepository;
import com.ultrahpm.productservice.search.ProductSearchRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class ProductServiceApplicationTests {

    @MockBean
    private ProductRepository productRepository;

    @MockBean
    private ProductSearchRepository productSearchRepository;

    @Test
    void contextLoads() {
        // Validates that the Spring context successfully boots up
        // with mock beans replacing the Testcontainers dependencies.
    }

}
