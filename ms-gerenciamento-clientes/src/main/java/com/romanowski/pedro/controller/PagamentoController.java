package com.romanowski.pedro.controller;

import com.romanowski.pedro.controller.swagger.PagamentoControllerSwagger;
import com.romanowski.pedro.dto.request.PagamentoRequestDTO;
import com.romanowski.pedro.service.PagamentoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1")
public class PagamentoController implements PagamentoControllerSwagger {

    private final PagamentoService pagamentoService;

    public PagamentoController(PagamentoService pagamentoService) {
        this.pagamentoService = pagamentoService;
    }

    @Override
    public ResponseEntity<Void> realizarPagamento(Long idCliente, Long idReserva, PagamentoRequestDTO pagamentoRequestDTO) {
        pagamentoService.realizarPagamento(idCliente, idReserva, pagamentoRequestDTO.valor());
        return ResponseEntity.status(HttpStatus.OK).body(null);
    }
}
