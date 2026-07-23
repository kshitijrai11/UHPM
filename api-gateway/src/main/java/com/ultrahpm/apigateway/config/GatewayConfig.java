package com.ultrahpm.apigateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // Product Service Routes
                .route("product-service", r -> r
                        .path("/api/products/**")
                        .filters(f -> f
                                .circuitBreaker(config -> config
                                        .setName("product-service-cb")
                                        .setFallbackUri("forward:/fallback/product-service"))
                                .retry(retryConfig -> retryConfig
                                        .setRetries(3)
                                        .setMethods(org.springframework.http.HttpMethod.GET))
                                .requestRateLimiter(config -> config
                                        .setRateLimiter(redisRateLimiter())
                                        .setKeyResolver(exchange ->
                                                reactor.core.publisher.Mono.just(
                                                        exchange.getRequest().getRemoteAddress().getAddress().getHostAddress())))
                        )
                        .uri("lb://product-service"))

                // Order Service Routes
                .route("order-service", r -> r
                        .path("/api/orders/**")
                        .filters(f -> f
                                .circuitBreaker(config -> config
                                        .setName("order-service-cb")
                                        .setFallbackUri("forward:/fallback/order-service"))
                                .retry(retryConfig -> retryConfig
                                        .setRetries(2)
                                        .setMethods(org.springframework.http.HttpMethod.GET,
                                                org.springframework.http.HttpMethod.POST))
                        )
                        .uri("lb://order-service"))

                // Payment Service Routes
                .route("payment-service", r -> r
                        .path("/api/payments/**")
                        .filters(f -> f
                                .circuitBreaker(config -> config
                                        .setName("payment-service-cb")
                                        .setFallbackUri("forward:/fallback/payment-service"))
                        )
                        .uri("lb://payment-service"))

                // Notification Service Routes
                .route("notification-service", r -> r
                        .path("/api/notifications/**")
                        .filters(f -> f
                                .circuitBreaker(config -> config
                                        .setName("notification-service-cb")
                                        .setFallbackUri("forward:/fallback/notification-service"))
                        )
                        .uri("lb://notification-service"))

                // Health Check Routes
                .route("health-check", r -> r
                        .path("/health/**")
                        .uri("lb://eureka-server"))
                .build();
    }

    @Bean
    public org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter redisRateLimiter() {
        return new org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter(100, 200, 1);
    }

    @Bean
    @org.springframework.context.annotation.Primary
    public org.springframework.cloud.gateway.filter.ratelimit.KeyResolver userKeyResolver() {
        return exchange -> reactor.core.publisher.Mono.just(
            exchange.getRequest().getRemoteAddress() != null 
                ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress() 
                : "unknown-user"
        );
    }

    @Bean
    public org.springframework.cloud.gateway.filter.ratelimit.KeyResolver pathKeyResolver() {
        return exchange -> reactor.core.publisher.Mono.just(
            exchange.getRequest().getPath().value()
        );
    }
}
