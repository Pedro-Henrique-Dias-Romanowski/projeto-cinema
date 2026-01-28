package com.romanowski.pedro.controller.swagger;

import com.romanowski.pedro.dto.request.PagamentoRequestDTO;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

public interface PagamentoControllerSwagger {

    @PostMapping("/pagamentos/{idCliente}/{idReserva}")
    ResponseEntity<Void> realizarPagamento(@PathVariable Long idCliente, @PathVariable Long idReserva, @Valid @RequestBody PagamentoRequestDTO pagamentoRequestDTO);
}
