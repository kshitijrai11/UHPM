package com.ultrahpm.apigateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class ApiGatewayApplication {

    public static void main(String[] args) {
        System.out.println("Starting UltraHPM API Gateway...");
        System.out.println("High-Performance Reactive Gateway with Circuit Breakers & Security");
        SpringApplication.run(ApiGatewayApplication.class, args);
        System.out.println("UltraHPM API Gateway is Ready!");
    }
}
