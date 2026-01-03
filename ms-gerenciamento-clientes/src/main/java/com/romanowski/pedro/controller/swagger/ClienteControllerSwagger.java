package com.romanowski.pedro.controller.swagger;

import com.romanowski.pedro.dto.request.ClienteRequestDTO;
import com.romanowski.pedro.dto.response.ClienteResponseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

public interface ClienteControllerSwagger {

    @PostMapping("/clientes")
    ResponseEntity<ClienteResponseDTO> cadastrarCliente(@RequestBody ClienteRequestDTO clienteRequestDTO);

    @GetMapping("/clientes")
    ResponseEntity<List<ClienteResponseDTO>> listarClientes();

    @GetMapping("/clientes/{id}")
    ResponseEntity<ClienteResponseDTO> buscarClientePorId(@PathVariable Long id);

    @DeleteMapping("/clientes/{id}")
    ResponseEntity<Void> deletarCliente(@PathVariable Long id);
}
