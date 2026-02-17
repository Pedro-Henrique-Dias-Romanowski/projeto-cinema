package com.romanowski.pedro.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public record ClienteResponseDTO(
        @JsonProperty("id")
        UUID idCliente,
        @JsonProperty("nome")
        String nomeCliente,
        @JsonProperty("email")
        String emailCliente
) {
}
