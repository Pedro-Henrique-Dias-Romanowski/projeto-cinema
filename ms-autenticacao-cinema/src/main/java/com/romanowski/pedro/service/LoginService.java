package com.romanowski.pedro.service;

import com.romanowski.pedro.dto.response.ClienteResponseDTO;
import com.romanowski.pedro.entity.ClienteEntity;
import com.romanowski.pedro.feign.ClienteFeignClient;
import com.romanowski.pedro.repository.AdministradorRepository;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class LoginService implements UserDetailsService {

    private final AdministradorRepository administradorRepository;
    private final ClienteFeignClient clienteFeignClient;
    private Logger logger = LoggerFactory.getLogger(TokenService.class);

    public LoginService(AdministradorRepository administradorRepository, ClienteFeignClient clienteFeignClient) {
        this.administradorRepository = administradorRepository;
        this.clienteFeignClient = clienteFeignClient;
    }

    @Override
    public UserDetails loadUserByUsername(@NonNull String username) throws UsernameNotFoundException {
        logger.info("Procurando dados do login do usuário: {}", username);
        ClienteResponseDTO clienteResponseDTO = clienteFeignClient.obterClientePorId(Long.valueOf(username)).get();
        // todo continuar implementação do login, verificar se é cliente ou administrador
        return null;
    }

    public String gerarTokenCliente(ClienteEntity cliente) {
        // todo implementar geração de token JWT para cliente
        return null;
    };

}
