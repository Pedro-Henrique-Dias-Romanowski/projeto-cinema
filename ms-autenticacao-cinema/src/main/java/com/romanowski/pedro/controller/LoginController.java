package com.romanowski.pedro.controller;

import com.romanowski.pedro.controller.swagger.LoginControllerSwagger;
import com.romanowski.pedro.dto.request.LoginRequestDTO;
import com.romanowski.pedro.dto.response.LoginResponseDTO;
import com.romanowski.pedro.entity.ClienteEntity;
import com.romanowski.pedro.service.LoginService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public ResponseEntity<LoginResponseDTO> efetuarLogin(LoginRequestDTO loginRequestDTO) throws Exception {
        // todo implementar login
        var authenticationToken = new UsernamePasswordAuthenticationToken(loginRequestDTO.id(), loginRequestDTO.senha());
        var authentication = authenticationManager.authenticate(authenticationToken);
        String token = loginService.gerarTokenCliente((ClienteEntity) authentication.getPrincipal());
        return null;
    }
}
