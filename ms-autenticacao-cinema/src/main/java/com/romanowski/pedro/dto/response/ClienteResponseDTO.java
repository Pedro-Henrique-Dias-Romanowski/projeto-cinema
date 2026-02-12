package com.romanowski.pedro.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ClienteResponseDTO(
        @JsonProperty("id")
        Long id,
        @JsonProperty("nome")
        String nome,
        @JsonProperty("email")
        String email
) {}
