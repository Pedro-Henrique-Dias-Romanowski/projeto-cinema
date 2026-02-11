package com.romanowski.pedro.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;

@OpenAPIDefinition(info =
@io.swagger.v3.oas.annotations.info.Info(title = "ms-gerenciamento-clientes API",
        version = "v1",
        description = "Documentation of ms-gerenciamento-clientes API"))
public class OpenApiConfiguration {

    @Bean
    public OpenAPI bibliotecaVirtualOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Cinecom")
                        .description("Documentação da ms-gerenciamento-clientes")
                        .version("1.0.0"));
    }
}
