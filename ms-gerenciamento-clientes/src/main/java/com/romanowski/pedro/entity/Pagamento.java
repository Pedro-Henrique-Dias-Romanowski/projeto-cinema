package com.romanowski.pedro.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Builder
public class Pagamento {
    private Long idReserva;
    private UUID idCliente;
    private Double valor;
}
