package com.romanowski.pedro.controller.swagger;

import com.romanowski.pedro.dto.request.LoginRequestDTO;
import com.romanowski.pedro.dto.response.LoginResponseDTO;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

public interface LoginControllerSwagger {

    @PostMapping("/auth/clientes/login")
    ResponseEntity<LoginResponseDTO> efetuarLoginCliente(@Valid  @RequestBody LoginRequestDTO loginRequestDTO) throws Exception;

    @PostMapping("/auth/administradores/login")
    ResponseEntity<LoginResponseDTO> efetuarLoginAdministradores(@Valid @RequestBody LoginRequestDTO loginRequestDTO) throws Exception;
}
