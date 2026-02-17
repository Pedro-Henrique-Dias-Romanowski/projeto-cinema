package com.romanowski.pedro.controller.swagger;

import com.romanowski.pedro.dto.request.ClienteRequestDTO;
import com.romanowski.pedro.dto.response.ClienteResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Cliente", description = "Operações disponíveis para clientes do cinema.")
public interface CadastroClienteControllerSwagger {

    @Operation(summary = "Cadastrar cliente", description = "Permite o cadastro de um novo cliente no cinema.")
    @PostMapping("/auth/clientes")
    ResponseEntity<ClienteResponseDTO> cadastrarCliente(@Valid @RequestBody ClienteRequestDTO clienteRequestDTO);
}
