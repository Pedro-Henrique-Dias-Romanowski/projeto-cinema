package com.romanowski.pedro.entity;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StatusPagamento {

    private Long idCliente;
    private Long idReserva;
    private Double valor;
    private String mensagem;
}
