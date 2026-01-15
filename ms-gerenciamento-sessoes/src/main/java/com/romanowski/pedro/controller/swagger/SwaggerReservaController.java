package com.romanowski.pedro.controller.swagger;

import com.romanowski.pedro.dto.response.ReservaResponseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

public interface SwaggerReservaController {

    @PostMapping("/reservas/{idSessao}/{idCliente}" )
    ResponseEntity<ReservaResponseDTO> criarReserva(@PathVariable Long idCliente, @PathVariable Long idSessao);
}
