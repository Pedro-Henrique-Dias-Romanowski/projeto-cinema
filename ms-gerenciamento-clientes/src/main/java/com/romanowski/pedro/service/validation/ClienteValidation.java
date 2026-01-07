package com.romanowski.pedro.service.validation;

import com.romanowski.pedro.entity.Cliente;
import com.romanowski.pedro.exceptions.ClienteInexistenteException;
import com.romanowski.pedro.exceptions.EmailExistenteException;
import com.romanowski.pedro.exceptions.ListaClientesVaziaException;
import com.romanowski.pedro.exceptions.SenhaInvalidaExcpetion;
import com.romanowski.pedro.repository.ClienteRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ClienteValidation {

    private final ClienteRepository clienteRepository;

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
            throw new ListaClientesVaziaException(mensagemListaVazia);
        }
    }

    public void validarBuscaPorCliente(Long id){
        if (clienteRepository.findById(id).isEmpty()){
            throw new ClienteInexistenteException(mensagemClienteInexistente);
        }
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
