package com.romanowski.pedro.dto.response;

public record ReservaResponseDTO(
        Long id,
        Long idCliente,
        Long idSessao,
        Boolean pagamentoConfirmado,
        Boolean ativa,
        String mensagem
) {
}
