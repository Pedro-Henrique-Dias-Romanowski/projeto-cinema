package com.romanowski.pedro.controller;

import com.romanowski.pedro.controller.swagger.CadastroClienteControllerSwagger;
import com.romanowski.pedro.dto.request.ClienteRequestDTO;
import com.romanowski.pedro.dto.response.ClienteResponseDTO;
import com.romanowski.pedro.mapper.ClienteMapper;
import com.romanowski.pedro.service.CadastroClienteService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1")
public class CadastroClienteController implements CadastroClienteControllerSwagger {

    private final ClienteMapper clienteMapper;
    private final CadastroClienteService cadastroClienteService;

    public CadastroClienteController(ClienteMapper clienteMapper, CadastroClienteService cadastroClienteService) {
        this.clienteMapper = clienteMapper;
        this.cadastroClienteService = cadastroClienteService;
    }

    @Override
    public ResponseEntity<ClienteResponseDTO> cadastrarCliente(ClienteRequestDTO clienteRequestDTO) {
        var clienteEntity = clienteMapper.toEntity(clienteRequestDTO);
        var clienteSalvo = cadastroClienteService.cadastrarCliente(clienteEntity);
        return ResponseEntity.status(HttpStatus.OK).body(clienteMapper.toResponseDTO(clienteSalvo));
    }
}
