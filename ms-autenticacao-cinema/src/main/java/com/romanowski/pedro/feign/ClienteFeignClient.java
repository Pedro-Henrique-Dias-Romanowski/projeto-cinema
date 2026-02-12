package com.romanowski.pedro.feign;

import com.romanowski.pedro.dto.response.ClienteResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Optional;

@FeignClient(name = "cliente-service", url = "${URL_CLIENTE_SERVICE}")
public interface ClienteFeignClient {

    @GetMapping("/v1/clientes/{id}")
    Optional<ClienteResponseDTO> obterClientePorId(@PathVariable Long id);
}
