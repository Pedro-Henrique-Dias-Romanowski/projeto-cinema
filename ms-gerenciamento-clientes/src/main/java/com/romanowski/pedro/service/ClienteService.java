package com.romanowski.pedro.service;

import com.romanowski.pedro.entity.Cliente;
import com.romanowski.pedro.repository.ClienteRepository;
import com.romanowski.pedro.service.email.EmailService;
import com.romanowski.pedro.service.validation.ClienteValidation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
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

    private final EmailService emailService;

    @Value("${mensagem.boasvindas.cinema.email}")
    private String mensagemCadastroClienteEmail;

    @Value("${mensagem.exclusao.cinema.email}")
    private String mensagemExclusaoClienteEmail;

    public ClienteService(ClienteRepository clienteRepository, ClienteValidation clienteValidation, PasswordEncoder passwordEncoder, EmailService emailService) {
        this.clienteRepository = clienteRepository;
        this.clienteValidation = clienteValidation;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }


    public Cliente cadastrarCliente(Cliente cliente){
        logger.info("Iniciando cadastro do cliente com email: {}", cliente.getEmail());
        clienteValidation.validarCadastroCliente(cliente);
        String senhaCriptografada = passwordEncoder.encode(cliente.getSenha());
        cliente.setSenha(senhaCriptografada);
        var clienteSalvo = clienteRepository.save(cliente);
        emailService.enviarEmail(cliente.getEmail(), "Bem-vindo ao Cinema", mensagemCadastroClienteEmail + cliente.getNome());
        return clienteSalvo;
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
        var cliente = clienteRepository.findById(id).get();
        clienteRepository.deleteById(id);
        emailService.enviarEmail(cliente.getEmail(), "Tchau, até a próxima", mensagemExclusaoClienteEmail + cliente.getNome());
    }
}
