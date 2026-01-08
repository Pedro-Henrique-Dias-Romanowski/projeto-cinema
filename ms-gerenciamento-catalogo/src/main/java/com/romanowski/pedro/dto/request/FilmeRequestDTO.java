package com.romanowski.pedro.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record FilmeRequestDTO(

        @NotBlank
        String titulo,
        @Min(60)
        @Max(240)
        @NotNull
        Integer duracao,
        @NotBlank
        String genero,
        @NotBlank
        String autor,

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy")
        LocalDate dataLancamento
) {
}
