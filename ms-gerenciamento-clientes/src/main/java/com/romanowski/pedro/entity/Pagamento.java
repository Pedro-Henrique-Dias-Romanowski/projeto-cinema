package com.romanowski.pedro.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class Pagamento {
    private Long idReserva;
    private Long idCliente;
    private Double valor;
}
