package com.romanowski.pedro.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public record SessaoResponseDTO(
        Long idSessao,
        Integer sala,
        String tituloFilme,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy HH:mm")
        LocalDateTime dataHoraSessao,
        Double preco,
        Boolean ativa
) {
}
