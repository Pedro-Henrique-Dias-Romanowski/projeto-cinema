package com.romanowski.pedro.mapper;

import com.romanowski.pedro.dto.request.FilmeAtualizacaoRequestDTO;
import com.romanowski.pedro.dto.request.FilmeRequestDTO;
import com.romanowski.pedro.dto.response.FilmeResponseDTO;
import com.romanowski.pedro.entity.Filme;
import org.mapstruct.Mapper;

import java.util.Optional;

@Mapper(componentModel = "spring")
public interface FilmeMapper {

    Filme toEntity(FilmeRequestDTO filmeRequestDTO);
    Filme toEntity(FilmeAtualizacaoRequestDTO filmeAtualizacaoRequestDTO);
    FilmeResponseDTO toResponseDTO(Filme filme);
    default FilmeResponseDTO entityToResponseDTO(Optional<Filme> livroEntity){
        return livroEntity.map(this::toResponseDTO).orElse(null);
    }
}
