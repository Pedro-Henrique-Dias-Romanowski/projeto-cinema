package com.romanowski.pedro.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.time.LocalDate;

public record FilmeAtualizacaoRequestDTO(
        String titulo,
        @Min(60)
        @Max(240)
        Integer duracao,
        String genero,
        String autor,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy")
        LocalDate dataLancamento
) {
}
