package com.romanowski.pedro.controller.swagger;

import com.romanowski.pedro.dto.request.LoginRequestDTO;
import com.romanowski.pedro.dto.response.LoginResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@Tag(name = "Login", description = "Operações de login para clientes e administradores do cinema.")
public interface LoginControllerSwagger {

    @Operation(summary = "Login Cliente", description = "Permite que o cliente realize seu login dentro do cinema.")
    @PostMapping("/auth/clientes/login")
    ResponseEntity<LoginResponseDTO> efetuarLoginCliente(@Valid  @RequestBody LoginRequestDTO loginRequestDTO) throws Exception;

    @Operation(summary = "Login Administrador", description = "Permite que o administrador realize seu login dentro do cinema.")
    @PostMapping("/auth/administradores/login")
    ResponseEntity<LoginResponseDTO> efetuarLoginAdministradores(@Valid @RequestBody LoginRequestDTO loginRequestDTO) throws Exception;
}
