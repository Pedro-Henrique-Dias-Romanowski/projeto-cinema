package com.romanowski.pedro.controller.swagger;

import com.romanowski.pedro.dto.request.FilmeAtualizacaoRequestDTO;
import com.romanowski.pedro.dto.request.FilmeRequestDTO;
import com.romanowski.pedro.dto.response.FilmeResponseDTO;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

public interface SwaggerFilmeController {

    @PostMapping("/filmes")
    ResponseEntity<FilmeResponseDTO> cadastrarFilme(@Valid @RequestBody FilmeRequestDTO filmeRequestDTO);

    @GetMapping("/filmes")
    ResponseEntity<List<FilmeResponseDTO>> listarFilmes();

    @GetMapping("/filmes/{id}")
    ResponseEntity<FilmeResponseDTO> buscarFilmePorId(@PathVariable Long id);

    @GetMapping("/filmes/titulo")
    ResponseEntity<FilmeResponseDTO> buscarFilmePorTitulo(@RequestHeader String titulo);

    @PatchMapping("/filmes/{id}")
    ResponseEntity<FilmeResponseDTO> atualizarFilme(@PathVariable Long id, @RequestBody FilmeAtualizacaoRequestDTO filmeAtualizacaoRequestDTO);

    @DeleteMapping("/filmes/{id}")
    ResponseEntity<Void> deletarFilme(@PathVariable Long id);
}
