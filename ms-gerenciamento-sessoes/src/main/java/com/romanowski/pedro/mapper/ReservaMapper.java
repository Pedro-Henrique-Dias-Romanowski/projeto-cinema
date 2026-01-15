package com.romanowski.pedro.mapper;

import com.romanowski.pedro.dto.response.ReservaResponseDTO;
import com.romanowski.pedro.entity.Reserva;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ReservaMapper {

    @Mapping(source = "sessao.id", target = "idSessao")
    ReservaResponseDTO toResponseDTO(Reserva reserva);
}
