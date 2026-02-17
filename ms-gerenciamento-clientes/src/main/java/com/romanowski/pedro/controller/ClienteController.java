package com.romanowski.pedro.controller;

import com.romanowski.pedro.controller.swagger.ClienteControllerSwagger;
import com.romanowski.pedro.dto.request.ClienteRequestDTO;
import com.romanowski.pedro.dto.response.ClienteResponseDTO;
import com.romanowski.pedro.entity.Cliente;
import com.romanowski.pedro.mapper.ClienteMapper;
import com.romanowski.pedro.service.ClienteService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1")
public class ClienteController implements ClienteControllerSwagger {

    private final ClienteMapper clienteMapper;

    private final ClienteService clienteService;

    public ClienteController(ClienteMapper clienteMapper, ClienteService clienteService) {
        this.clienteMapper = clienteMapper;
        this.clienteService = clienteService;
    }

    @Override
    public ResponseEntity<ClienteResponseDTO> cadastrarCliente(ClienteRequestDTO clienteRequestDTO) {
        var clienteEntity = clienteMapper.toEntity(clienteRequestDTO);
        var clienteSalvo = clienteService.cadastrarCliente(clienteEntity);
        return ResponseEntity.status(HttpStatus.OK).body(clienteMapper.toResponseDTO(clienteSalvo));
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ClienteResponseDTO>> listarClientes() {
        List<Cliente> clientes = clienteService.listarClientes();
        var clientesResponseDTO = clientes.stream()
                .map(clienteMapper::toResponseDTO)
                .toList();
        return ResponseEntity.status(HttpStatus.OK).body(clientesResponseDTO);
    }

    @Override
    @PreAuthorize(
            "hasRole('ADMIN') or " +
            "(hasRole('CLIENTE') and #id.toString() == authentication.principal.subject)"
    )
    public ResponseEntity<ClienteResponseDTO> buscarClientePorId(UUID id) {
        var cliente = clienteService.buscarClientePorId(id);
        return cliente.isPresent() ? ResponseEntity.status(HttpStatus.OK).body(clienteMapper.entityToResponseDTO(cliente)) : ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
    }

    @Override
    @PreAuthorize(
            "(hasRole('CLIENTE') and #id.toString() == authentication.principal.subject)"
    )
    public ResponseEntity<Void> deletarCliente(UUID id) {
        clienteService.deletarCliente(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
    }
}
