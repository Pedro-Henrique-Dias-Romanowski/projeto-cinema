package com.romanowski.pedro.controller;

import com.romanowski.pedro.controller.swagger.SwaggerSessaoController;
import com.romanowski.pedro.dto.request.SessaoRequestDTO;
import com.romanowski.pedro.dto.response.SessaoResponseDTO;
import com.romanowski.pedro.entity.Sessao;
import com.romanowski.pedro.mapper.SessaoMapper;
import com.romanowski.pedro.service.SessaoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/v1")
public class SessaoController implements SwaggerSessaoController {

    private final SessaoMapper sessaoMapper;
    private final SessaoService sessaoService;

    public SessaoController(SessaoMapper sessaoMapper, SessaoService sessaoService) {
        this.sessaoMapper = sessaoMapper;
        this.sessaoService = sessaoService;
    }


    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SessaoResponseDTO> cadastrarSessao(SessaoRequestDTO sessaoRequestDTO) {
        Sessao sessao = sessaoMapper.toEntity(sessaoRequestDTO);
        Sessao sessaoSalva = sessaoService.cadastrarSessao(sessao);
        return ResponseEntity.status(HttpStatus.OK).body(sessaoMapper.toResponseDTO(sessaoSalva));
    }

    @Override
    @PreAuthorize("hasAnyRole('CLIENTE', 'ADMIN')")
    public ResponseEntity<List<SessaoResponseDTO>> listarSessoes() {
        List<Sessao> sessoes = sessaoService.listarSessoes();
        List<SessaoResponseDTO> sessoesResponseDTO = sessoes.stream().map(sessaoMapper::toResponseDTO).toList();
        return ResponseEntity.status(HttpStatus.OK).body(sessoesResponseDTO);
    }

    @Override
    @PreAuthorize("hasAnyRole('CLIENTE', 'ADMIN')")
    public ResponseEntity<SessaoResponseDTO> procurarSessaoPorId(Long id) {
        Optional<Sessao> sessaoEncontrada = sessaoService.procurarSessaoPorId(id);
        return ResponseEntity.status(HttpStatus.OK).body(sessaoMapper.toResponseDTO(sessaoEncontrada.orElse(null)));
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> cancelarSessao(Long idSessao) {
        sessaoService.cancelarSessao(idSessao);
        return ResponseEntity.status(HttpStatus.OK).body(null);
    }
}
