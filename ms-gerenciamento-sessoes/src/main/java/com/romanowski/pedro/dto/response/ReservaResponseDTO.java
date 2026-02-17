package com.romanowski.pedro.dto.response;

import java.util.UUID;

public record ReservaResponseDTO(
        Long id,
        UUID idCliente,
        Long idSessao,
        Boolean pagamentoConfirmado,
        Boolean ativa,
        String mensagem
) {
}
