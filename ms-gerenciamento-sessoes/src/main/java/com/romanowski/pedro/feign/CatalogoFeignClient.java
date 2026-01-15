package com.romanowski.pedro.feign;

import com.romanowski.pedro.config.FeignInterceptor;
import com.romanowski.pedro.dto.response.FilmeResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Optional;

@FeignClient(name = "catalogo-service", url = "${URL_CATALOGO_SERVICE}", configuration = FeignInterceptor.class)
public interface CatalogoFeignClient {

    @GetMapping("v1/filmes/titulo")
    Optional<FilmeResponseDTO> obterFilmePorTitulo();
}
