package com.romanowski.pedro.entity;

import jakarta.persistence.*;
import jakarta.transaction.Transactional;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@EqualsAndHashCode(of = "id")
@Table(name = "reserva")
@Entity
public class Reserva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "id_cliente", nullable = false)
    private Long idCliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_sessao", nullable = false)
    private Sessao sessao;

    @Column(name = "pagamento_confirmado")
    private Boolean pagamentoConfirmado;

    @Column
    private Boolean ativa;

    @Column
    private String mensagem;
}
