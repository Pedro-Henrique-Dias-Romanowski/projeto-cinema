package com.romanowski.pedro.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.romanowski.pedro.entity.AdministradorEntity;
import com.romanowski.pedro.entity.ClienteEntity;
import com.romanowski.pedro.enums.Perfil;
import com.romanowski.pedro.repository.AdministradorRepository;
import com.romanowski.pedro.repository.ClienteRepository;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;

@Service
public class LoginService implements UserDetailsService {

    @Value("${spring.security.jwt.secret}")
    private String JWT_SECRET;

    private final AdministradorRepository administradorRepository;
    private final ClienteRepository clienteRepository;
    private Logger logger = LoggerFactory.getLogger(TokenService.class);

    public LoginService(AdministradorRepository administradorRepository, ClienteRepository clienteRepository) {
        this.administradorRepository = administradorRepository;
        this.clienteRepository = clienteRepository;
    }

    @Override
    public UserDetails loadUserByUsername(@NonNull String username) throws UsernameNotFoundException {
        logger.info("Procurando dados do login do usuário: {}", username);
        return clienteRepository.findByEmailIgnoreCase(username)
                .map(user -> (UserDetails) user)
                .or(() -> administradorRepository.findByEmailIgnoreCase(username)
                        .map(user -> (UserDetails) user))
                .orElseThrow(() -> new UsernameNotFoundException("Usuario nao encontrado"));
    }

    public String gerarTokenCliente(ClienteEntity cliente){
        try {
            Algorithm algorithm = Algorithm.HMAC256(JWT_SECRET);

            return JWT.create()
                    .withIssuer("cinecom-auth")
                    .withSubject(cliente.getId().toString())
                    .withClaim("email", cliente.getEmail())
                    .withClaim("roles", List.of("CLIENTE"))
                    .withIssuedAt(new Date())
                    .withExpiresAt(dataExpiracaoToken())
                    .sign(algorithm);

        } catch (JWTCreationException e){
            throw new RuntimeException("Erro ao gerar token JWT", e);
        }
    }

    public String gerarTokenAdministrador(AdministradorEntity administrador){
        try{
            Algorithm algorithm = Algorithm.HMAC256(JWT_SECRET);
            return JWT.create()
                    .withIssuer("cinecom-auth")
                    .withSubject(administrador.getId().toString())
                    .withClaim("email", administrador.getEmail())
                    .withClaim("roles", List.of("ADMIN"))
                    .withIssuedAt(new Date())
                    .withExpiresAt(dataExpiracaoToken())
                    .sign(algorithm);
        } catch(JWTCreationException e){
            throw new RuntimeException("Erro ao gerar token JWT", e);
        }
    }

    private Instant dataExpiracaoToken(){
        return LocalDateTime.now().plusMinutes(30).toInstant(ZoneOffset.of("-03:00"));
    }

}
