package com.romanowski.pedro.controller.swagger;

import com.romanowski.pedro.dto.response.ReservaResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;
import java.util.UUID;

@Tag(name = "Reserva", description = "Operações disponíveis para as reservas de sessões do cinema.")
public interface SwaggerReservaController {

    @Operation(summary = "Criar reserva", description = "Permite a criação de uma nova reserva para uma sessão específica por um cliente.")
    @PostMapping("/reservas/{idCliente}/{idSessao}" )
    ResponseEntity<ReservaResponseDTO> criarReserva(@PathVariable UUID idCliente, @PathVariable Long idSessao);


    @Operation(summary = "Listar reservas", description = "Permite a listagem de todas as reservas feitas por um cliente específico.")
    @GetMapping("/reservas/{idCliente}")
    ResponseEntity<List<ReservaResponseDTO>> listarReservas(@PathVariable UUID idCliente);

    @Operation(summary = "Buscar reserva por id", description = "Permite a busca de uma reserva específica por se ID e o ID do cliente.")
    @GetMapping("/reservas/{idCliente}/{idReserva}")
    ResponseEntity<ReservaResponseDTO> buscarReservaPorId(@PathVariable UUID idCliente, @PathVariable Long idReserva);

    @Operation(summary = "Cancelamento de reserva", description = "Permite o cancelamento (exclusão lógica) de uma reserva específica por seu ID e o ID do cliente.")
    @DeleteMapping("/reservas/{idCliente}/{idReserva}")
    ResponseEntity<Void> cancelarReserva(@PathVariable UUID idCliente, @PathVariable Long idReserva);
}
