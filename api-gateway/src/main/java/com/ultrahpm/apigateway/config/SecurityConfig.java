package com.ultrahpm.apigateway.config;

import com.ultrahpm.apigateway.filter.AuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    private final AuthenticationFilter authenticationFilter;

    public SecurityConfig(AuthenticationFilter authenticationFilter) {
        this.authenticationFilter = authenticationFilter;
    }

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers(
                                "/actuator/**",
                                "/health/**",
                                "/fallback/**",
                                "/api/auth/**",
                                "/public/**"
                        ).permitAll()
                        .anyExchange().authenticated()
                )
                .addFilterBefore(authenticationFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                .build();
    }
}
