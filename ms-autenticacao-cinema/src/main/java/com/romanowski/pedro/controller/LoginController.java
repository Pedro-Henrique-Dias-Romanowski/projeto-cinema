package com.romanowski.pedro.controller;

import com.romanowski.pedro.controller.swagger.LoginControllerSwagger;
import com.romanowski.pedro.dto.request.LoginRequestDTO;
import com.romanowski.pedro.dto.response.LoginResponseDTO;
import com.romanowski.pedro.entity.AdministradorEntity;
import com.romanowski.pedro.entity.ClienteEntity;
import com.romanowski.pedro.service.LoginService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/v1")
public class LoginController implements LoginControllerSwagger {


    private final AuthenticationManager authenticationManager;

    private final LoginService loginService;

    public LoginController(AuthenticationManager authenticationManager, LoginService loginService) {
        this.authenticationManager = authenticationManager;
        this.loginService = loginService;
    }

    @Override
    public ResponseEntity<LoginResponseDTO> efetuarLoginCliente(LoginRequestDTO loginRequestDTO) {
        var authenticationToken = new UsernamePasswordAuthenticationToken(loginRequestDTO.email(), loginRequestDTO.senha());
        var authentication = authenticationManager.authenticate(authenticationToken);
        String token = loginService.gerarTokenCliente((ClienteEntity) authentication.getPrincipal());

        return ResponseEntity.ok().body(new LoginResponseDTO(token, LocalDateTime.now(), ((ClienteEntity) authentication.getPrincipal()).getPerfil(), ((ClienteEntity) authentication.getPrincipal()).getId()));
    }


    @Override
    public ResponseEntity<LoginResponseDTO> efetuarLoginAdministradores(LoginRequestDTO loginRequestDTO) throws Exception {
        var authenticationToken = new UsernamePasswordAuthenticationToken(loginRequestDTO.email(), loginRequestDTO.senha());
        var authentication = authenticationManager.authenticate(authenticationToken);
        String token = loginService.gerarTokenAdministrador((AdministradorEntity) authentication.getPrincipal());

        return ResponseEntity.ok().body(new LoginResponseDTO(token, LocalDateTime.now(), ((AdministradorEntity) authentication.getPrincipal()).getPerfil(), ((AdministradorEntity) authentication.getPrincipal()).getId()));
    }
}
