package com.romanowski.pedro.feign;

import com.romanowski.pedro.dto.response.ReservaResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "ms-gerenciamento-sessoes", url = "${URL_RESERVA_SERVICE}")
public interface ReservaFeignClient {

    @GetMapping("v1/reservas/{idCliente}/{idReserva}")
    ReservaResponseDTO buscarReservaPorId(@PathVariable UUID idCliente, @PathVariable Long idReserva);
}
