package com.romanowski.pedro.controller.swagger;

import com.romanowski.pedro.dto.response.ReservaResponseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

public interface SwaggerReservaController {

    @PostMapping("/reservas/{idSessao}/{idCliente}" )
    ResponseEntity<ReservaResponseDTO> criarReserva(@PathVariable Long idCliente, @PathVariable Long idSessao);

    @GetMapping("/reservas/{idCliente}")
    ResponseEntity<List<ReservaResponseDTO>> listarReservas(@PathVariable Long idCliente);

    @GetMapping("/reservas/{idCliente}/{idReserva}")
    ResponseEntity<ReservaResponseDTO> buscarReservaPorId(@PathVariable Long idCliente, @PathVariable Long idReserva);

    @DeleteMapping("/reservas/{id}")
    ResponseEntity<Void> cancelarReserva(@PathVariable Long id);
}
