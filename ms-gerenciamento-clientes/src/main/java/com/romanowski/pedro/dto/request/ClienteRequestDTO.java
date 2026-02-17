package com.romanowski.pedro.dto.request;

import jakarta.validation.constraints.*;

import java.util.UUID;

public record ClienteRequestDTO(

        @NotNull
        UUID id,

        @NotBlank
        String nome,

        @Email
        @NotBlank
        String email,

        @NotBlank
        String senha,

        @NotNull
        @PositiveOrZero
        @DecimalMax("1000.0")
        Double saldo
) {
}
