package com.romanowski.pedro.feign;

import com.romanowski.pedro.config.FeignInterceptor;
import com.romanowski.pedro.dto.response.FilmeResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Optional;

@FeignClient(name = "catalogo-service",configuration = FeignInterceptor.class)
public interface CatalogoFeignClient {

    @GetMapping("v1/filmes/titulo")
    Optional<FilmeResponseDTO> obterFilmePorTitulo();
}
