package com.romanowski.pedro.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record LoginRequestDTO(
        @NotBlank String email,
        @NotBlank String senha
) {
}
