package com.romanowski.pedro.controller.swagger;

import com.romanowski.pedro.dto.request.SessaoRequestDTO;
import com.romanowski.pedro.dto.response.SessaoResponseDTO;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

public interface SwaggerSessaoController {

    @PostMapping("/sessoes")
    ResponseEntity<SessaoResponseDTO> fazerReservaSessao(@Valid @RequestBody SessaoRequestDTO sessaoRequestDTO);

    @PostMapping("/sessoes/confirmar/{id}")
    ResponseEntity<SessaoResponseDTO> confirmarReservaSessao(@PathVariable Long id);

    @GetMapping("/sessoes")
    ResponseEntity<List<SessaoResponseDTO>> listarSessoes();

    @GetMapping("/sessoes/{id}")
    ResponseEntity<SessaoResponseDTO> procurarSessaoPorId(Long id);

    @DeleteMapping("sessoes/{idCliente}/{idSessao}")
    ResponseEntity<Void> cancelarReservaSessao(@PathVariable Long idSessao, @PathVariable Long idCliente);
}
