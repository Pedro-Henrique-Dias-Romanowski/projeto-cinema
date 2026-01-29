package com.romanowski.pedro.mapper;

import com.romanowski.pedro.dto.response.FilmeResponseDTO;
import com.romanowski.pedro.dto.response.ReservaResponseDTO;
import com.romanowski.pedro.dto.response.StatusPagamentoResponseDTO;
import com.romanowski.pedro.entity.Reserva;
import com.romanowski.pedro.entity.StatusPagamento;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Optional;

@Mapper(componentModel = "spring")
public interface ReservaMapper {

    @Mapping(source = "sessao.id", target = "idSessao")
    ReservaResponseDTO toResponseDTO(Reserva reserva);
    StatusPagamentoResponseDTO toStatusPagamentoResponseDTO(StatusPagamento statusPagamento);
    StatusPagamento toStatusPagamento(StatusPagamentoResponseDTO statusPagamento);
    default ReservaResponseDTO entityToResponseDTO(Optional<Reserva> reservaEntity){
        return reservaEntity.map(this::toResponseDTO).orElse(null);
    }
}
