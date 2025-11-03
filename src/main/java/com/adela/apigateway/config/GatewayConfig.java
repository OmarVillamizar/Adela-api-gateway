package com.adela.apigateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.reactive.ServerHttpRequest;

@Configuration
public class GatewayConfig {
    
    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
            .route("ms-auth", r -> r.path("/ms-auth/**", "/auth/**", "/oauth2/**", "/login/**", "/logout/**")
                .filters(f -> f
                    .filter((exchange, chain) -> {
                        // Asegurar que el header Authorization se pase
                        ServerHttpRequest request = exchange.getRequest();
                        String authHeader = request.getHeaders().getFirst("Authorization");
                        
                        if (authHeader != null) {
                            ServerHttpRequest modifiedRequest = request.mutate()
                                .header("Authorization", authHeader)
                                .build();
                            exchange = exchange.mutate().request(modifiedRequest).build();
                        }
                        
                        return chain.filter(exchange);
                    })
                )
                .uri("lb://ms-auth"))
            .build();
    }
}