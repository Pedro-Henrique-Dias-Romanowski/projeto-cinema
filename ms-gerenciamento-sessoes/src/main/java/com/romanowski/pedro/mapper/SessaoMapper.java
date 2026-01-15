package com.romanowski.pedro.mapper;

import com.romanowski.pedro.dto.request.SessaoRequestDTO;
import com.romanowski.pedro.dto.response.SessaoResponseDTO;
import com.romanowski.pedro.entity.Sessao;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Optional;

@Mapper(componentModel = "spring")
public interface SessaoMapper {

    Sessao toEntity(SessaoRequestDTO sessaoRequestDTO);

    @Mapping(source = "id", target = "idSessao")
    SessaoResponseDTO toResponseDTO(Sessao sessao);
    default SessaoResponseDTO entityToResponseDTO(Optional<Sessao> sessaoEntity){
        return sessaoEntity.map(this::toResponseDTO).orElse(null);
    }
}
