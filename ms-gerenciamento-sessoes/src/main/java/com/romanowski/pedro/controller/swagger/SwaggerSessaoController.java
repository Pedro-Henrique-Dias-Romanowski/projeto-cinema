package com.romanowski.pedro.controller.swagger;

import com.romanowski.pedro.dto.request.SessaoRequestDTO;
import com.romanowski.pedro.dto.response.SessaoResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Sessão", description = "Operações disponíveis para as sessões do cinema.")
public interface SwaggerSessaoController {


    @Operation(summary = "Cadastrar sessão", description = "Permite o cadastro de uma nova sessão no cinema.")
    @PostMapping("/sessoes")
    ResponseEntity<SessaoResponseDTO> cadastrarSessao(@Valid @RequestBody SessaoRequestDTO sessaoRequestDTO);

    @Operation(summary = "Listar sessões", description = "Permite a listagem de todas as sessões disponíveis no cinema.")
    @GetMapping("/sessoes")
    ResponseEntity<List<SessaoResponseDTO>> listarSessoes();

    @Operation(summary = "Buscar sessão por ID", description = "Permite a busca de uma sessão específica por seu ID.")
    @GetMapping("/sessoes/{id}")
    ResponseEntity<SessaoResponseDTO> procurarSessaoPorId(@PathVariable Long id);

    @Operation(summary = "Cancelar sessão", description = "Permite o cancelamento (exclusão lógica) de uma sessão específica por seu ID.")
    @DeleteMapping("sessoes/{idSessao}")
    ResponseEntity<Void> cancelarSessao(@PathVariable Long idSessao);
}
