package com.romanowski.pedro.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public record ClienteResponseDTO(
        @JsonProperty("id")
        UUID id,
        @JsonProperty("nome")
        String nome,
        @JsonProperty("email")
        String email
) {}
