package com.romanowski.pedro.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record ClienteRequestDTO(

        @NotBlank
        String nome,

        @Email
        @NotBlank
        String email,

        String senha,

        @NotBlank
        @Min(0)
        @Max(1000)
        Double saldo
) {
}
