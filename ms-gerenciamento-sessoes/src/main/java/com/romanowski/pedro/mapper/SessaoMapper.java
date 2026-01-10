package com.romanowski.pedro.mapper;

import com.romanowski.pedro.dto.request.SessaoRequestDTO;
import com.romanowski.pedro.dto.response.SessaoResponseDTO;
import com.romanowski.pedro.entity.Sessao;
import org.mapstruct.Mapper;

import java.util.Optional;

@Mapper(componentModel = "spring")
public interface SessaoMapper {

    Sessao toEntity(SessaoRequestDTO sessaoRequestDTO);
    SessaoResponseDTO toResponseDTO(Sessao sessao);
    default SessaoResponseDTO entityToResponseDTO(Optional<Sessao> sessaoEntity){
        return sessaoEntity.map(this::toResponseDTO).orElse(null);
    }
}
