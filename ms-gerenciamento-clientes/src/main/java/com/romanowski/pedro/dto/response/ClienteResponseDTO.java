package com.romanowski.pedro.dto.response;

import java.util.UUID;

public record ClienteResponseDTO(

        UUID id,
        String nome,
        String email,
        Double saldo
) {
}
