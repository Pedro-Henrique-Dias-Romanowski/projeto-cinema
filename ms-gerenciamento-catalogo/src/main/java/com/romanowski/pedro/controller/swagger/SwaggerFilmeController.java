package com.romanowski.pedro.controller.swagger;

import com.romanowski.pedro.dto.request.FilmeAtualizacaoRequestDTO;
import com.romanowski.pedro.dto.request.FilmeRequestDTO;
import com.romanowski.pedro.dto.response.FilmeResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Filme", description = "Operações disponíveis para os filmes do cinema.")
public interface SwaggerFilmeController {

    @Operation(summary = "Cadastrar filme", description = "Permite o cadastro de um novo filme na biblioteca.")
    @PostMapping("/filmes")
    ResponseEntity<FilmeResponseDTO> cadastrarFilme(@Valid @RequestBody FilmeRequestDTO filmeRequestDTO);

    @Operation(summary = "Listar filmes", description = "Permite a listagem de todos os filmes disponíveis na biblioteca.")
    @GetMapping("/filmes")
    ResponseEntity<List<FilmeResponseDTO>> listarFilmes();

    @Operation(summary = "Buscar filme por id", description = "Permite a busca de um filme específico por seu ID.")
    @GetMapping("/filmes/{id}")
    ResponseEntity<FilmeResponseDTO> buscarFilmePorId(@PathVariable Long id);

    @Operation(summary = "Buscar filme por título", description = "Permite a busca de um filme específico por seu título.")
    @GetMapping("/filmes/titulo")
    ResponseEntity<FilmeResponseDTO> buscarFilmePorTitulo(@RequestHeader String titulo);

    @Operation(summary = "Atualizar filme", description = "Permite a atualização dos dados de um filme específico por seu ID.")
    @PatchMapping("/filmes/{id}")
    ResponseEntity<FilmeResponseDTO> atualizarFilme(@PathVariable Long id, @RequestBody FilmeAtualizacaoRequestDTO filmeAtualizacaoRequestDTO);

    @Operation(summary = "Exclusão de um filme", description = "Permite a exclusão de um filme específico por seu ID.")
    @DeleteMapping("/filmes/{id}")
    ResponseEntity<Void> deletarFilme(@PathVariable Long id);
}
