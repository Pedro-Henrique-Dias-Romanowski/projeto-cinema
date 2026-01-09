package com.romanowski.pedro.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;

public record FilmeResponseDTO(
        Long id,
        String titulo,
        Integer duracao,
        String genero,
        String autor,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy")
        LocalDate dataLancamento
) {
}
