package com.romanowski.pedro.dto.response;

public record ClienteResponseDTO(

        Long id,
        String nome,
        String email,
        Double saldo
) {
}
