package com.romanowski.pedro.entity;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@EqualsAndHashCode(of = "id")
@Table(name = "sessoes")
@Entity
public class Sessao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "id_filme", nullable = false)
    private Long idFilme;

    @Transient
    private String tituloFilme;

    @OneToMany(mappedBy = "sessao", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Reserva> reservas;

    Integer sala;

    private Double preco;

    @Column(name = "data_hora", nullable = false)
    private LocalDateTime dataHoraSessao;
}
