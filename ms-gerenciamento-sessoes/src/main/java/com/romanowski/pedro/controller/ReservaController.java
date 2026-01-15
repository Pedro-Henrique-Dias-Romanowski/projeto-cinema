package com.romanowski.pedro.controller;

import com.romanowski.pedro.controller.swagger.SwaggerReservaController;
import com.romanowski.pedro.dto.response.ReservaResponseDTO;
import com.romanowski.pedro.entity.Reserva;
import com.romanowski.pedro.mapper.ReservaMapper;
import com.romanowski.pedro.service.ReservaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1")
public class ReservaController implements SwaggerReservaController {

    private final ReservaService reservaService;

    private final ReservaMapper reservaMapper;

    public ReservaController(ReservaService reservaService, ReservaMapper reservaMapper) {
        this.reservaService = reservaService;
        this.reservaMapper = reservaMapper;
    }

    @Override
    public ResponseEntity<ReservaResponseDTO> criarReserva(Long idCliente, Long idSessao) {
        Reserva reserva = reservaService.adicionarReserva(idCliente, idSessao);
        return ResponseEntity.status(HttpStatus.OK).body(reservaMapper.toResponseDTO(reserva));
    }
}
