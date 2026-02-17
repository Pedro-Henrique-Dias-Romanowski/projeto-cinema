package com.romanowski.pedro.dto.response;

import java.util.UUID;

public record StatusPagamentoResponseDTO(
        UUID idCliente,
        Long idReserva,
        Double valor
) {
}
