package com.romanowski.pedro.service;

import com.romanowski.pedro.entity.ClienteEntity;
import com.romanowski.pedro.enums.Perfil;
import com.romanowski.pedro.feign.ClienteFeignClient;
import com.romanowski.pedro.mapper.ClienteMapper;
import com.romanowski.pedro.repository.ClienteRepository;
import com.romanowski.pedro.service.validation.CadastroClienteValidation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class CadastroClienteService {

    private static final Logger logger = LoggerFactory.getLogger(CadastroClienteService.class);

    private final PasswordEncoder passwordEncoder;
    private final ClienteRepository clienteRepository;
    private final CadastroClienteValidation cadastroClienteValidation;
    private final ClienteFeignClient clienteFeignClient;
    private final ClienteMapper clienteMapper;

    public CadastroClienteService(PasswordEncoder passwordEncoder, ClienteRepository clienteRepository, CadastroClienteValidation cadastroClienteValidation, ClienteFeignClient clienteFeignClient, ClienteMapper clienteMapper) {
        this.passwordEncoder = passwordEncoder;
        this.clienteRepository = clienteRepository;
        this.cadastroClienteValidation = cadastroClienteValidation;
        this.clienteFeignClient = clienteFeignClient;
        this.clienteMapper = clienteMapper;
    }

    public ClienteEntity cadastrarCliente(ClienteEntity cliente){
        logger.info("Iniciando cadastro do cliente com email: {}", cliente.getEmail());
        cadastroClienteValidation.validarCadastroCliente(cliente);
        String senhaCriptografada = passwordEncoder.encode(cliente.getSenha());
        cliente.setSenha(senhaCriptografada);
        cliente.setPerfil(Perfil.CLIENTE);
        var clienteSalvo = clienteRepository.save(cliente);
        var clienteRequestDTO = clienteMapper.toDTO(clienteSalvo);
        clienteFeignClient.cadastrarCliente(clienteRequestDTO);
        return clienteSalvo;
    }
}
