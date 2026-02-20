package com.romanowski.pedro.controller;

import com.romanowski.pedro.controller.swagger.SwaggerFilmeController;
import com.romanowski.pedro.dto.request.FilmeAtualizacaoRequestDTO;
import com.romanowski.pedro.dto.request.FilmeRequestDTO;
import com.romanowski.pedro.dto.response.FilmeResponseDTO;
import com.romanowski.pedro.entity.Filme;
import com.romanowski.pedro.mapper.FilmeMapper;
import com.romanowski.pedro.service.FilmeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/v1")
public class CatalogoController implements SwaggerFilmeController {

    private final FilmeService filmeService;
    private final FilmeMapper filmeMapper;

    public CatalogoController(FilmeService filmeService, FilmeMapper filmeMapper) {
        this.filmeService = filmeService;
        this.filmeMapper = filmeMapper;
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FilmeResponseDTO> cadastrarFilme(FilmeRequestDTO filmeRequestDTO) {
        Filme filmeEntity = filmeMapper.toEntity(filmeRequestDTO);
        Filme filmeSalvo = filmeService.cadastrarFilme(filmeEntity);
        return ResponseEntity.status(HttpStatus.CREATED).body(filmeMapper.toResponseDTO(filmeSalvo));
    }

    @Override
    @PreAuthorize("hasAnyRole('CLIENTE', 'ADMIN')")
    public ResponseEntity<List<FilmeResponseDTO>> listarFilmes() {
        List<Filme> filmes = filmeService.listarFilmes();
        List<FilmeResponseDTO> filmeResponseDTOs = filmes.stream().map(filmeMapper::toResponseDTO).toList();
        return ResponseEntity.status(HttpStatus.OK).body(filmeResponseDTOs);
    }

    @Override
    @PreAuthorize("hasAnyRole('CLIENTE', 'ADMIN')")
    public ResponseEntity<FilmeResponseDTO> buscarFilmePorId(Long id) {
        Optional<Filme> filme = filmeService.buscarFilmePorId(id);
        return ResponseEntity.status(HttpStatus.OK).body(filmeMapper.toResponseDTO(filme.orElse(null)));
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FilmeResponseDTO> buscarFilmePorTitulo(String titulo) {
        Optional<Filme> filme = filmeService.buscarFilmePorTitulo(titulo);
        return ResponseEntity.status(HttpStatus.OK).body(filmeMapper.toResponseDTO(filme.orElse(null)));
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FilmeResponseDTO> atualizarFilme(Long id, FilmeAtualizacaoRequestDTO filmeAtualizacaoRequestDTO) {
        Filme filmeParaSerAtualizado = filmeMapper.toEntity(filmeAtualizacaoRequestDTO);
        Filme filmeAtualizado = filmeService.atualizarFilme(id, filmeParaSerAtualizado);
        return ResponseEntity.status(HttpStatus.OK).body(filmeMapper.toResponseDTO(filmeAtualizado));
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deletarFilme(Long id) {
        filmeService.deletarFilme(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
    }
}
