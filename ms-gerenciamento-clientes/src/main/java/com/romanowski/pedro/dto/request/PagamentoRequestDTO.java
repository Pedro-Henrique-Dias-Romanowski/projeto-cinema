package com.romanowski.pedro.dto.request;

import jakarta.validation.constraints.NotNull;

public record PagamentoRequestDTO(
        @NotNull
        Double valor
) {
}
