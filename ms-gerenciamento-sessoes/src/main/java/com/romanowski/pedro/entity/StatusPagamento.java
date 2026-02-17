package com.romanowski.pedro.entity;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StatusPagamento {

    private UUID idCliente;
    private Long idReserva;
    private Double valor;
    private String mensagem;
}
