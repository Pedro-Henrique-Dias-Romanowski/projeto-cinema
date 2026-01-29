package com.romanowski.pedro.dto.response;

public record StatusPagamentoResponseDTO(
        Long idCliente,
        Long idReserva,
        Double valor
) {
}
