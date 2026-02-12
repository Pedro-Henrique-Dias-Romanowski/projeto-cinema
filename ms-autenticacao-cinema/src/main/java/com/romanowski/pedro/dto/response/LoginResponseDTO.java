package com.romanowski.pedro.dto.response;

import com.romanowski.pedro.enums.Perfil;

import java.time.LocalDateTime;

public record LoginResponseDTO(
        String token,
        LocalDateTime dataHoraLogin,
        Perfil perfilAtribuido,
        Long id
) {
}
