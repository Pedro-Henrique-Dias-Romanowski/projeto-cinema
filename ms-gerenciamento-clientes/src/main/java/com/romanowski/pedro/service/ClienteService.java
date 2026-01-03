package com.romanowski.pedro.service;

import com.romanowski.pedro.entity.Cliente;
import com.romanowski.pedro.repository.ClienteRepository;
import com.romanowski.pedro.service.validation.ClienteValidation;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;

    private final ClienteValidation clienteValidation;

    private final PasswordEncoder passwordEncoder;

    public ClienteService(ClienteRepository clienteRepository, ClienteValidation clienteValidation, PasswordEncoder passwordEncoder) {
        this.clienteRepository = clienteRepository;
        this.clienteValidation = clienteValidation;
        this.passwordEncoder = passwordEncoder;
    }


    public Cliente cadastrarCliente(Cliente cliente){
        clienteValidation.validarCadastroCliente(cliente);
        String senhaCriptografada = passwordEncoder.encode(cliente.getSenha());
        cliente.setSenha(senhaCriptografada);
        return clienteRepository.save(cliente);
    }

    public Cliente buscarClientePorId(Long id){
        return null;
    }

    public List<Cliente> listarClientes(){
        clienteValidation.validarListagemClientes();
        return clienteRepository.findAll();
    }

    public void deletarCliente(Long id){

    }
}
