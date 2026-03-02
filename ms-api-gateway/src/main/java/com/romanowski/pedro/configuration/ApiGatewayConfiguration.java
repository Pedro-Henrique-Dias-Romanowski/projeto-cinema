package com.romanowski.pedro.configuration;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApiGatewayConfiguration {

    @Bean
    public RouteLocator gatewayRouter(RouteLocatorBuilder builder){
        return builder.routes()
                .route(p -> p.path("/auth/**").uri("lb://ms-autenticacao-cinema"))
                .route(p -> p.path("/catalogo/**").uri("lb://ms-gerenciamento-catalogo"))
                .route(p -> p.path("/clientes/**").uri("lb://ms-gerenciamento-clientes"))
                .route(p -> p.path("/sessoes/**").uri("lb://ms-gerenciamento-sessoes"))
                .build();
    }
}
