package com.romanowski.pedro.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;

public record FilmeResponseDTO(
        @JsonProperty("id")
        Long idFilme,
        String titulo,
        Integer duracao,
        String genero,
        String autor,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy")
        LocalDate dataLancamento
) {
}
