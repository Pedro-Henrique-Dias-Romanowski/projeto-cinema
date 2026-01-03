package com.romanowski.pedro.controller;

import com.romanowski.pedro.controller.swagger.ClienteControllerSwagger;
import com.romanowski.pedro.dto.request.ClienteRequestDTO;
import com.romanowski.pedro.dto.response.ClienteResponseDTO;
import com.romanowski.pedro.mapper.ClienteMapper;
import com.romanowski.pedro.service.ClienteService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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
        // TODO implementar a use case de salvar o cliente
        return ResponseEntity.status(HttpStatus.CREATED).body(clienteMapper.toResponseDTO(clienteEntity));
    }

    @Override
    public ResponseEntity<List<ClienteResponseDTO>> listarClientes() {
        // TODO implementar a use case de listar os clientes
        return null;
    }

    @Override
    public ResponseEntity<ClienteResponseDTO> buscarClientePorId(Long id) {
        // TODO implementar a use case de buscar cliente por ID
        return null;
    }

    @Override
    public ResponseEntity<Void> deletarCliente(Long id) {
        // TODO implementar a use case de deletar cliente
        return null;
    }
}
