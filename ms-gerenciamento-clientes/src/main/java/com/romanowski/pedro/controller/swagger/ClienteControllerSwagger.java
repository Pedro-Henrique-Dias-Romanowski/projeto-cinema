package com.romanowski.pedro.controller.swagger;

import com.romanowski.pedro.dto.request.ClienteRequestDTO;
import com.romanowski.pedro.dto.response.ClienteResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Cliente", description = "Operações disponíveis para clientes do cinema.")
public interface ClienteControllerSwagger {

    @Operation(summary = "Cadastrar cliente", description = "Permite o cadastro de um novo cliente no cinema.")
    @PostMapping("/clientes")
    ResponseEntity<ClienteResponseDTO> cadastrarCliente(@Valid @RequestBody ClienteRequestDTO clienteRequestDTO);

    @Operation(summary = "Listar clientes", description = "Permite listar os clientes que estão cadastrados dentro do cinema.")
    @GetMapping("/clientes")
    ResponseEntity<List<ClienteResponseDTO>> listarClientes();

    @Operation(summary = "Buscar cliente por ID", description = "Permite a busca por um cliente específico através do seu ID.")
    @GetMapping("/clientes/{id}")
    ResponseEntity<ClienteResponseDTO> buscarClientePorId(@PathVariable @Valid Long id);

    @Operation(summary = "Exclusão de um cliente", description = "Permite a exclusão de um cliente específico através do seu ID.")
    @ResponseStatus(code = org.springframework.http.HttpStatus.NO_CONTENT)
    @DeleteMapping("/clientes/{id}")
    ResponseEntity<Void> deletarCliente(@PathVariable Long id);
}
