package com.romanowski.pedro.service.validation;

import com.romanowski.pedro.entity.ClienteEntity;
import com.romanowski.pedro.exceptions.EmailExistenteException;
import com.romanowski.pedro.exceptions.SenhaInvalidaException;
import com.romanowski.pedro.repository.ClienteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CadastroClienteValidation {

    private static final Logger logger = LoggerFactory.getLogger(CadastroClienteValidation.class);

    private final ClienteRepository clienteRepository;

    @Value("${mensagem.email.existente}")
    private String mensagemEmailExistente;

    @Value("${mensagem.senha.invalida}")
    private String mensagemSenhaInvalida;

    public CadastroClienteValidation(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    public void validarCadastroCliente(ClienteEntity cliente){
        validarExistenciaEmail(cliente.getEmail());
        validarSenhaCliente(cliente.getSenha());
    }

    private void validarExistenciaEmail(String email){
        if (clienteRepository.existsByEmail(email)){
            logger.info("Cliente com email {} encontrado", email);
            throw new EmailExistenteException(mensagemEmailExistente);
        }
    }

    private void validarSenhaCliente(String senha){
        if (senha.length() <= 5 || senha.length() > 15){
            logger.error("Senha inválida");
            throw new SenhaInvalidaException(mensagemSenhaInvalida);
        }
    }
}
