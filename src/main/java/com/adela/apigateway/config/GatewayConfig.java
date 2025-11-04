package com.adela.apigateway.config;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class GatewayConfig {
    
    private GatewayFilter authHeaderFilter() {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String authHeader = request.getHeaders().getFirst("Authorization");
            
            if (authHeader != null) {
                ServerHttpRequest modifiedRequest = request.mutate()
                    .header("Authorization", authHeader)
                    .build();
                exchange = exchange.mutate().request(modifiedRequest).build();
            }
            
            return chain.filter(exchange);
        };
    }
    
    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
            // Ruta existente ms-auth
            .route("ms-auth", r -> r.path("/ms-auth/**", "/auth/**", "/oauth2/**", "/login/**", "/logout/**")
                .filters(f -> f.filter(authHeaderFilter()))
                .uri("lb://ms-auth"))
            
            // ← NUEVA RUTA PARA MS-GRUPOS
            .route("ms-grupos", r -> r.path("/ms-grupos/**", "/api/grupos/**")
                .filters(f -> f.filter(authHeaderFilter()))
                .uri("lb://ms-grupos"))
            .build();
    }
    
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.addAllowedOriginPattern("*");
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

}