package com.romanowski.pedro.service.validation;

import com.romanowski.pedro.entity.Cliente;
import com.romanowski.pedro.exceptions.ClienteNuloException;
import com.romanowski.pedro.exceptions.EmailExistenteException;
import com.romanowski.pedro.exceptions.SenhaInvalidaExcpetion;
import com.romanowski.pedro.repository.ClienteRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ClienteValidation {

    private final ClienteRepository clienteRepository;

    @Value("${cliente.email.existente}")
    private String mensagemEmailExistente;

    @Value("${cliente.objeto.nulo}")
    private String mensagemClienteNulo;

    @Value("${cliente.senha.invalida}")
    private String mensagemSenhaInvalida;

    public ClienteValidation(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    public void validarCadastroCliente(Cliente cliente){
        validarExistenciaEmail(cliente.getEmail());
        validarSenhaCliente(cliente.getSenha());
    }


    private void validarExistenciaEmail(String email){
        if (clienteRepository.existsByEmail(email)){
            throw new EmailExistenteException(mensagemEmailExistente);
        }
    }

    private void validarSenhaCliente(String senha){
        if (senha.length() <= 5 || senha.length() > 15){
            throw new SenhaInvalidaExcpetion(mensagemSenhaInvalida);
        }
    }
}
