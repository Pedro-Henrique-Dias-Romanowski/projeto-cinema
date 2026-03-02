package com.romanowski.pedro.configuration;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public List<GroupedOpenApi> apis(RouteDefinitionLocator locator) {
        List<GroupedOpenApi> groups = new ArrayList<>();
        List<RouteDefinition> definitions = locator.getRouteDefinitions().collectList().block();
        
        if (definitions != null) {
            definitions.stream()
                .filter(routeDefinition -> routeDefinition.getId().matches("autenticacao|catalogo|clientes|sessoes"))
                .forEach(routeDefinition -> {
                    String name = routeDefinition.getId();
                    groups.add(GroupedOpenApi.builder()
                        .pathsToMatch("/" + getPathPrefix(name) + "/**")
                        .group(getDisplayName(name))
                        .build());
                });
        }
        
        return groups;
    }
    
    private String getPathPrefix(String routeId) {
        return switch (routeId) {
            case "autenticacao" -> "auth";
            case "catalogo" -> "filmes";
            case "clientes" -> "clientes";
            case "sessoes" -> "sessoes";
            default -> routeId;
        };
    }
    
    private String getDisplayName(String routeId) {
        return switch (routeId) {
            case "autenticacao" -> "Autenticação API";
            case "catalogo" -> "Catálogo API";
            case "clientes" -> "Clientes API";
            case "sessoes" -> "Sessões API";
            default -> routeId;
        };
    }
}
