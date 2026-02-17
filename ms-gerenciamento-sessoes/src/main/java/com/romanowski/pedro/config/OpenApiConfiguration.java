package com.romanowski.pedro.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(info =
@io.swagger.v3.oas.annotations.info.Info(title = "ms-gerenciamento-sessoes API",
        version = "v1",
        description = "Documentação da Microserviço de Gerenciamento de Sessões e Reservas do Cinema"))
public class OpenApiConfiguration {

    @Bean
    public OpenAPI bibliotecaVirtualOpenAPI() {
        return new OpenAPI()
                .info(new io.swagger.v3.oas.models.info.Info()
                        .title("Cinecom - Gerenciamento Sessões e Reservas")
                        .description("Documentação do ms-gerenciamento-sessoes")
                        .version("1.0.0"))
                .addSecurityItem(new SecurityRequirement().addList("bearer-jwt"))
                .components(new Components()
                        .addSecuritySchemes("bearer-jwt", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .in(SecurityScheme.In.HEADER)
                                .name("Authorization")));
    }
}
