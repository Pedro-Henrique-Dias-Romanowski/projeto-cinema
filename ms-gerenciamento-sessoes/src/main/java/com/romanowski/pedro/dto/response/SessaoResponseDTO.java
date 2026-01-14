package com.romanowski.pedro.dto.response;

public record SessaoResponseDTO(
        Long idCliente,
        Long idSessao,
        Integer sala,
        String tituloFilme,
        String mensagem
) {
}
