package com.ultrahpm.productservice.grpc;

import com.ultrahpm.productservice.dto.ProductDTO;
import com.ultrahpm.productservice.service.ProductService;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;

import java.util.List;

@GrpcService
public class ProductGrpcServiceImpl extends ProductGrpcServiceGrpc.ProductGrpcServiceImplBase {

    private final ProductService productService;

    public ProductGrpcServiceImpl(ProductService productService) {
        this.productService = productService;
    }

    @Override
    public void getProductsByIds(GetProductsRequest request, StreamObserver<GetProductsResponse> responseObserver) {
        List<ProductDTO> products = productService.getProductsByIds(request.getProductIdsList());
        
        GetProductsResponse.Builder responseBuilder = GetProductsResponse.newBuilder();
        
        for (ProductDTO dto : products) {
            ProductGrpc grpcProduct = ProductGrpc.newBuilder()
                    .setId(dto.id())
                    .setName(dto.name())
                    .setCategory(dto.category())
                    .setPrice(dto.price().doubleValue())
                    .setStockQuantity(dto.stockQuantity())
                    .setImageUrl(dto.imageUrl() != null ? dto.imageUrl() : "")
                    .build();
            responseBuilder.addProducts(grpcProduct);
        }

        responseObserver.onNext(responseBuilder.build());
        responseObserver.onCompleted();
    }
}
