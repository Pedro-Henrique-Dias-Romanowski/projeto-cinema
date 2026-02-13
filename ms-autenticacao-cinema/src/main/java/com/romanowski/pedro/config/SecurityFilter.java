package com.romanowski.pedro.config;

import com.romanowski.pedro.repository.AdministradorRepository;
import com.romanowski.pedro.repository.ClienteRepository;
import com.romanowski.pedro.service.TokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class SecurityFilter extends OncePerRequestFilter {

    private final ClienteRepository clienteRepository;
    private final AdministradorRepository administradorRepository;
    private final TokenService tokenService;

    public SecurityFilter(ClienteRepository clienteRepository, AdministradorRepository administradorRepository, TokenService tokenService) {
        this.clienteRepository = clienteRepository;
        this.administradorRepository = administradorRepository;
        this.tokenService = tokenService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String token = recuperarTokenRequisicao(request);

        if (token != null){
            String email = tokenService.verificarToken(token);

            var usuario = clienteRepository.findByEmailIgnoreCase(email)
                    .map(user -> (UserDetails) user)
                    .or (() -> administradorRepository.findByEmailIgnoreCase(email)
                            .map(user -> (UserDetails) user))
                    .orElse(null);

            if (usuario != null) {
                Authentication authentication = new UsernamePasswordAuthenticationToken(usuario, null, usuario.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }

        }

        filterChain.doFilter(request, response);
    }

    private String recuperarTokenRequisicao(HttpServletRequest request){
        var authorizationHeader = request.getHeader("Authorization");
        if(authorizationHeader != null){
            return authorizationHeader.replace("Bearer ", "");
        }
        return null;
    }
}
