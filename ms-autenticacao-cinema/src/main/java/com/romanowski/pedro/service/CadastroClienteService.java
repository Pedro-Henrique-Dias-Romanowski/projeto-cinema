package com.romanowski.pedro.service;

import com.romanowski.pedro.entity.ClienteEntity;
import com.romanowski.pedro.enums.Perfil;
import com.romanowski.pedro.feign.ClienteFeignClient;
import com.romanowski.pedro.mapper.ClienteMapper;
import com.romanowski.pedro.repository.ClienteRepository;
import com.romanowski.pedro.service.validation.CadastroClienteValidation;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.naming.ServiceUnavailableException;
import java.util.UUID;

@Service
public class CadastroClienteService {

    private static final Logger logger = LoggerFactory.getLogger(CadastroClienteService.class);

    private final PasswordEncoder passwordEncoder;
    private final ClienteRepository clienteRepository;
    private final CadastroClienteValidation cadastroClienteValidation;
    private final ClienteFeignClient clienteFeignClient;
    private final ClienteMapper clienteMapper;

    @Value("${ms.clientes.indisponivel}")
    private String mensagemFallbackCadastrarCliente;

    public CadastroClienteService(PasswordEncoder passwordEncoder, ClienteRepository clienteRepository, CadastroClienteValidation cadastroClienteValidation, ClienteFeignClient clienteFeignClient, ClienteMapper clienteMapper) {
        this.passwordEncoder = passwordEncoder;
        this.clienteRepository = clienteRepository;
        this.cadastroClienteValidation = cadastroClienteValidation;
        this.clienteFeignClient = clienteFeignClient;
        this.clienteMapper = clienteMapper;
    }

    @CircuitBreaker(name = "clienteService", fallbackMethod = "fallbackCadastrarCliente")
    @Retry(name = "clienteService", fallbackMethod = "fallbackCadastrarCliente")
    @RateLimiter(name = "clienteService")
    public ClienteEntity cadastrarCliente(ClienteEntity cliente){
        logger.info("Iniciando cadastro do cliente com email: {}", cliente.getEmail());
        cadastroClienteValidation.validarCadastroCliente(cliente);
        String senhaCriptografada = passwordEncoder.encode(cliente.getSenha());
        cliente.setSenha(senhaCriptografada);
        cliente.setPerfil(Perfil.CLIENTE);
        cliente.setId(UUID.randomUUID());
        var clienteSalvo = clienteRepository.save(cliente);
        var clienteRequestDTO = clienteMapper.toDTO(clienteSalvo);
        clienteFeignClient.cadastrarCliente(clienteRequestDTO);
        return clienteSalvo;
    }


    public ClienteEntity fallbackCadastrarCliente(ClienteEntity cliente, Throwable t) throws Exception {
        logger.error("Fallback acionado ao cadastrar cliente {}: {} - Tipo de erro: {}",
                    cliente.getEmail(),
                    t.getMessage(),
                    t.getClass().getSimpleName());
        throw new ServiceUnavailableException(mensagemFallbackCadastrarCliente);
    }
}
