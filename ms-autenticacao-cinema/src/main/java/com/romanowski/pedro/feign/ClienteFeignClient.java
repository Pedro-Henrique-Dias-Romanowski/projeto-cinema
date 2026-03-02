package com.romanowski.pedro.feign;

import com.romanowski.pedro.dto.request.CadastroFeignClientRequestDTO;
import com.romanowski.pedro.dto.response.ClienteResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Optional;

@FeignClient(name = "cliente-service")
public interface ClienteFeignClient {

    @GetMapping("/v1/clientes/{id}")
    Optional<ClienteResponseDTO> obterClientePorId(@PathVariable Long id);

    @PostMapping("/v1/clientes")
    ClienteResponseDTO cadastrarCliente(@RequestBody CadastroFeignClientRequestDTO clienteRequestDTO);
}
