package com.romanowski.pedro.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record SessaoRequestDTO(
        @NotBlank
        String tituloFilme,
        @Min(1)
        @Max(5)
        Integer sala,
        @NotNull
        @Min(15)
        @Max(70)
        Double preco,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy HH:mm")
        LocalDateTime dataHoraSessao
) {
}
