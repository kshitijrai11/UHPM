package com.ultrahpm.productservice.grpc;

import com.ultrahpm.productservice.dto.ProductDTO;
import com.ultrahpm.productservice.service.ProductService;
import io.grpc.ManagedChannel;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ProductGrpcServiceImplTest {

    @Mock
    private ProductService productService;

    @InjectMocks
    private ProductGrpcServiceImpl productGrpcService;

    private ManagedChannel channel;
    private io.grpc.Server server;

    @BeforeEach
    void setUp() throws Exception {
        String serverName = InProcessServerBuilder.generateName();
        server = InProcessServerBuilder
                .forName(serverName).directExecutor().addService(productGrpcService).build().start();
        channel = InProcessChannelBuilder.forName(serverName).directExecutor().build();
    }

    @AfterEach
    void tearDown() {
        channel.shutdownNow();
        server.shutdownNow();
    }

    @Test
    void testGetProductsByIds() {
        ProductDTO productDTO = new ProductDTO("1", "Laptop", "Electronics", new BigDecimal("999.99"), 10, "Description", "laptop.png", true, null, null);
        when(productService.getProductsByIds(anyList())).thenReturn(List.of(productDTO));

        ProductGrpcServiceGrpc.ProductGrpcServiceBlockingStub blockingStub = ProductGrpcServiceGrpc.newBlockingStub(channel);

        GetProductsRequest request = GetProductsRequest.newBuilder().addProductIds("1").build();
        GetProductsResponse response = blockingStub.getProductsByIds(request);

        assertEquals(1, response.getProductsCount());
        assertEquals("Laptop", response.getProducts(0).getName());
        assertEquals(999.99, response.getProducts(0).getPrice());
    }
}
