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
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ClienteService {

    private static final Logger logger = LoggerFactory.getLogger(ClienteService.class);

    private final ClienteRepository clienteRepository;

    private final ClienteValidation clienteValidation;

    private final PasswordEncoder passwordEncoder;

    private final EmailService emailService;

    @Value("${mensagem.boasvindas.cinema.email:}")
    private String mensagemCadastroClienteEmail;

    @Value("${mensagem.exclusao.cinema.email:}")
    private String mensagemExclusaoClienteEmail;

    public ClienteService(ClienteRepository clienteRepository, ClienteValidation clienteValidation, PasswordEncoder passwordEncoder, EmailService emailService) {
        this.clienteRepository = clienteRepository;
        this.clienteValidation = clienteValidation;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    private String formatarMensagem(String template, String nome, String fallback) {
        if (template == null || template.isBlank()) {
            return String.format(fallback, nome);
        }
        return String.format(template, nome);
    }

    @Transactional
    public Cliente cadastrarCliente(Cliente cliente){
        logger.info("Iniciando cadastro do cliente com email: {}", cliente.getEmail());
        clienteValidation.validarCadastroCliente(cliente);
        var clienteDB = Cliente.builder()
                .id(cliente.getId())
                .nome(cliente.getNome())
                .email(cliente.getEmail())
                .senha(cliente.getSenha())
                .saldo(cliente.getSaldo())
                .build();
        var clienteSalvo = clienteRepository.save(clienteDB);
        System.out.println("ID recebida: " + cliente.getId());
        var mensagem = formatarMensagem(mensagemCadastroClienteEmail, cliente.getNome(), "Bem-vindo(a), %s!");
        emailService.enviarEmail(cliente.getEmail(), "Bem-vindo ao Cinema", mensagem);
        return clienteSalvo;
    }

    @Transactional(readOnly = true)
    public Optional<Cliente> buscarClientePorId(UUID id){
        logger.info("Iniciando busca do cliente com id: {}", id);
        return clienteRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Cliente> listarClientes(){
        logger.info("Iniciando listagem de clientes");
        clienteValidation.validarListagemClientes();
        return clienteRepository.findAll();
    }

    @Transactional
    public void deletarCliente(UUID id){
        logger.info("Iniciando remoção do cliente com id: {}", id);
        clienteValidation.validarBuscaPorCliente(id);
        var cliente = clienteRepository.findById(id).get();
        clienteRepository.deleteById(id);
        var mensagem = formatarMensagem(mensagemExclusaoClienteEmail, cliente.getNome(), "Tchau, %s. Até a próxima!");
        emailService.enviarEmail(cliente.getEmail(), "Tchau, até a próxima", mensagem);
    }
}
