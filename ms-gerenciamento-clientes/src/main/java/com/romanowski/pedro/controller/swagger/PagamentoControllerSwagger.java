package com.romanowski.pedro.controller.swagger;

import com.romanowski.pedro.dto.request.PagamentoRequestDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.UUID;

@Tag(name = "Pagamento", description = "Possíveis transações relacionadas a pagamento no cinema.")
public interface PagamentoControllerSwagger {

    @Operation(summary = "Realizar o pagamento", description = "Permite a realização do pagamento de uma reserva feita por um cliente.")
    @PostMapping("/pagamentos/{idCliente}/{idReserva}")
    ResponseEntity<Void> realizarPagamento(@PathVariable UUID idCliente, @PathVariable Long idReserva, @Valid @RequestBody PagamentoRequestDTO pagamentoRequestDTO);
}
