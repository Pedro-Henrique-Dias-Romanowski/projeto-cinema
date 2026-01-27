package com.romanowski.pedro.service.validation;

import com.romanowski.pedro.entity.Cliente;
import com.romanowski.pedro.exceptions.ClienteInexistenteException;
import com.romanowski.pedro.exceptions.EmailExistenteException;
import com.romanowski.pedro.exceptions.ListaClientesVaziaException;
import com.romanowski.pedro.exceptions.SenhaInvalidaExcpetion;
import com.romanowski.pedro.repository.ClienteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ClienteValidation {

    private final ClienteRepository clienteRepository;

    private static final Logger logger = LoggerFactory.getLogger(ClienteValidation.class);

    @Value("${cliente.email.existente}")
    private String mensagemEmailExistente;

    @Value("${cliente.senha.invalida}")
    private String mensagemSenhaInvalida;

    @Value("${cliente.lista.vazia}")
    private String mensagemListaVazia;

    @Value("${cliente.inexistente}")
    private String mensagemClienteInexistente;

    public ClienteValidation(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    public void validarCadastroCliente(Cliente cliente){
        validarExistenciaEmail(cliente.getEmail());
        validarSenhaCliente(cliente.getSenha());
    }

    public void validarListagemClientes(){
        if (clienteRepository.findAll().isEmpty()) {
            logger.error("Nenhum cliente encontrado");
            throw new ListaClientesVaziaException(mensagemListaVazia);
        }
    }

    public void validarBuscaPorCliente(Long id){
        if (clienteRepository.findById(id).isEmpty()){
            logger.error("Cliente com id {} não encontrado", id);
            throw new ClienteInexistenteException(mensagemClienteInexistente);
        }
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
            throw new SenhaInvalidaExcpetion(mensagemSenhaInvalida);
        }
    }
}
