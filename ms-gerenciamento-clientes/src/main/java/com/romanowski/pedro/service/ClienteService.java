package com.romanowski.pedro.service;

import com.romanowski.pedro.Application;
import com.romanowski.pedro.entity.Cliente;
import com.romanowski.pedro.repository.ClienteRepository;
import com.romanowski.pedro.service.validation.ClienteValidation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ClienteService {

    private static final Logger logger = LoggerFactory.getLogger(ClienteService.class);

    private final ClienteRepository clienteRepository;

    private final ClienteValidation clienteValidation;

    private final PasswordEncoder passwordEncoder;

    public ClienteService(ClienteRepository clienteRepository, ClienteValidation clienteValidation, PasswordEncoder passwordEncoder) {
        this.clienteRepository = clienteRepository;
        this.clienteValidation = clienteValidation;
        this.passwordEncoder = passwordEncoder;
    }


    public Cliente cadastrarCliente(Cliente cliente){
        logger.info("Iniciando cadastro do cliente com email: {}", cliente.getEmail());
        clienteValidation.validarCadastroCliente(cliente);
        String senhaCriptografada = passwordEncoder.encode(cliente.getSenha());
        cliente.setSenha(senhaCriptografada);
        return clienteRepository.save(cliente);
    }

    public Optional<Cliente> buscarClientePorId(Long id){
        logger.info("Iniciando busca do cliente com id: {}", id);
        return clienteRepository.findById(id);
    }

    public List<Cliente> listarClientes(){
        logger.info("Iniciando listagem de clientes");
        clienteValidation.validarListagemClientes();
        return clienteRepository.findAll();
    }

    public void deletarCliente(Long id){
        logger.info("Iniciando remoção do cliente com id: {}", id);
        clienteValidation.validarBuscaPorCliente(id);
        clienteRepository.deleteById(id);
    }
}
