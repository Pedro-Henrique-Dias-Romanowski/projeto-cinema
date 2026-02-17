package com.romanowski.pedro.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;

import java.util.UUID;

import static java.sql.Types.VARCHAR;

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

    @Column(name = "id_cliente", nullable = false, length = 36, columnDefinition = "CHAR(36)")
    @JdbcTypeCode(VARCHAR)
    private UUID idCliente;

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
