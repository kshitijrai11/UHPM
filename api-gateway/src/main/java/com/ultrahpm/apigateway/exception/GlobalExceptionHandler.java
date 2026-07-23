package com.ultrahpm.apigateway.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Component
@Order(-1)
public class GlobalExceptionHandler implements ErrorWebExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        logger.error("❌ Global exception occurred: ", ex);

        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
        response.getHeaders().add("Content-Type", MediaType.APPLICATION_JSON_VALUE);

        String errorResponse = String.format(
                "{\"success\":false,\"error\":\"Internal Server Error\",\"message\":\"%s\",\"timestamp\":\"%s\"}",
                ex.getMessage() != null ? ex.getMessage().replace("\"", "'") : "Unknown error",
                LocalDateTime.now()
        );

        DataBuffer buffer = response.bufferFactory().wrap(errorResponse.getBytes());
        return response.writeWith(Mono.just(buffer));
    }
}
