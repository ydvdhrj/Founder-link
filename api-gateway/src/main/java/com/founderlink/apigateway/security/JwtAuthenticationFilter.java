package com.founderlink.apigateway.security;

import java.util.List;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private static final String BEARER_PREFIX = "Bearer ";
    private static final String USER_ID_HEADER = "X-User-Id";
    private static final List<String> PUBLIC_PATH_PREFIXES = List.of(
            "/auth/",
            "/swagger-ui",
            "/v3/api-docs",
            "/auth/v3/api-docs",
            "/users/v3/api-docs",
            "/startups/v3/api-docs",
            "/investments/v3/api-docs",
            "/teams/v3/api-docs",
            "/messages/v3/api-docs",
            "/notifications/v3/api-docs",
            "/actuator");

    private final JwtAuthenticationManager jwtAuthenticationManager;

    public JwtAuthenticationFilter(JwtAuthenticationManager jwtAuthenticationManager) {
        this.jwtAuthenticationManager = jwtAuthenticationManager;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        if (shouldSkip(exchange)) {
            return chain.filter(exchange);
        }

        String authorization = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authorization == null || !authorization.startsWith(BEARER_PREFIX)) {
            return unauthorized(exchange);
        }

        String token = authorization.substring(BEARER_PREFIX.length()).trim();
        if (token.isEmpty()) {
            return unauthorized(exchange);
        }

        Authentication tokenAuthentication = new UsernamePasswordAuthenticationToken(token, token);
        return jwtAuthenticationManager.authenticate(tokenAuthentication)
                .flatMap(authentication -> forwardWithUserId(exchange, chain, authentication))
                .onErrorResume(BadCredentialsException.class, e -> unauthorized(exchange));
    }

    private Mono<Void> forwardWithUserId(ServerWebExchange exchange, GatewayFilterChain chain, Authentication authentication) {
        String userId = authentication.getName();
        if (userId == null || userId.isBlank()) {
            return unauthorized(exchange);
        }

        ServerWebExchange mutated = exchange.mutate()
                .request(request -> request.headers(headers -> headers.set(USER_ID_HEADER, userId)))
                .build();
        return chain.filter(mutated);
    }

    private boolean shouldSkip(ServerWebExchange exchange) {
        String path = exchange.getRequest().getPath().value();
        HttpMethod method = exchange.getRequest().getMethod();
        if (method == HttpMethod.OPTIONS) {
            return true;
        }
        return PUBLIC_PATH_PREFIXES.stream().anyMatch(path::startsWith);
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 10;
    }
}
