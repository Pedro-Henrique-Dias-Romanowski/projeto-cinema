package com.romanowski.pedro.mapper;


import com.romanowski.pedro.dto.request.CadastroFeignClientRequestDTO;
import com.romanowski.pedro.dto.request.ClienteRequestDTO;
import com.romanowski.pedro.dto.response.ClienteResponseDTO;
import com.romanowski.pedro.entity.ClienteEntity;
import org.mapstruct.Mapper;

import java.util.Optional;

@Mapper(componentModel = "spring")
public interface ClienteMapper {

    ClienteEntity toEntity(ClienteRequestDTO dto);
    ClienteResponseDTO toResponseDTO(ClienteEntity cliente);
    CadastroFeignClientRequestDTO toDTO(ClienteEntity cliente);
    default ClienteResponseDTO entityToResponseDTO(Optional<ClienteEntity> clienteEntity){
        return clienteEntity.map(this::toResponseDTO).orElse(null);
    }
}
