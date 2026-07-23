package com.ultrahpm.apigateway.filter;

import com.ultrahpm.apigateway.service.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.Collections;

@Component
public class AuthenticationFilter implements WebFilter {

    private final JwtService jwtService;

    public AuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        // Skip authentication for public endpoints
        if (isPublicPath(path)) {
            return chain.filter(exchange);
        }

        String authHeader = request.getHeaders().getFirst("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            return jwtService.validateToken(token)
                    .flatMap(username -> {
                        if (username != null) {
                            Authentication auth = new UsernamePasswordAuthenticationToken(
                                    username, null, Collections.emptyList());
                            return chain.filter(exchange)
                                    .contextWrite(ReactiveSecurityContextHolder.withAuthentication(auth));
                        } else {
                            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                            return exchange.getResponse().setComplete();
                        }
                    })
                    .onErrorResume(throwable -> {
                        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                        return exchange.getResponse().setComplete();
                    });
        } else {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
    }

    private boolean isPublicPath(String path) {
        return path.startsWith("/actuator") ||
                path.startsWith("/health") ||
                path.startsWith("/fallback") ||
                path.startsWith("/api/auth") ||
                path.startsWith("/public");
    }
}
