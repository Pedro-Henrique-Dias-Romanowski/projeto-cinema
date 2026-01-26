package com.romanowski.pedro.controller.swagger;

import com.romanowski.pedro.dto.request.SessaoRequestDTO;
import com.romanowski.pedro.dto.response.SessaoResponseDTO;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

public interface SwaggerSessaoController {


    @PostMapping("/sessoes")
    ResponseEntity<SessaoResponseDTO> cadastrarSessao(@Valid @RequestBody SessaoRequestDTO sessaoRequestDTO);

    @GetMapping("/sessoes")
    ResponseEntity<List<SessaoResponseDTO>> listarSessoes();

    @GetMapping("/sessoes/{id}")
    ResponseEntity<SessaoResponseDTO> procurarSessaoPorId(@PathVariable Long id);

    @DeleteMapping("sessoes/{idSessao}")
    ResponseEntity<Void> cancelarSessao(@PathVariable Long idSessao);
}
