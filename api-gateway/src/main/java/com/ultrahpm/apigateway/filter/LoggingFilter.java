package com.ultrahpm.apigateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class LoggingFilter implements GlobalFilter, Ordered {

    private static final Logger logger = LoggerFactory.getLogger(LoggingFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        long startTime = System.currentTimeMillis();

        logger.info("🔄 Incoming request: {} {} from {}",
                request.getMethod(),
                request.getURI(),
                request.getRemoteAddress());

        return chain.filter(exchange)
                .doFinally(signalType -> {
                    long endTime = System.currentTimeMillis();
                    logger.info("✅ Request completed: {} {} - Status: {} - Duration: {}ms",
                            request.getMethod(),
                            request.getURI(),
                            exchange.getResponse().getStatusCode(),
                            (endTime - startTime));
                });
    }

    @Override
    public int getOrder() {
        return -1; // High priority
    }
}
