package com.romanowski.pedro.dto.request;

import jakarta.validation.constraints.*;

public record ClienteRequestDTO(

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
