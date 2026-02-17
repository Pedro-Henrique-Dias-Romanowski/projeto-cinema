package com.romanowski.pedro.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;

import java.util.List;
import java.util.UUID;

import static java.sql.Types.VARCHAR;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
@Entity
@Table(name = "clientes")
public class Cliente {

    @Id
    @Column(name = "id", length = 36, columnDefinition = "CHAR(36)")
    @JdbcTypeCode(VARCHAR)
    private UUID id;
    private String nome;
    private String email;
    private String senha;
    private Double saldo;

    @Transient
    private List<Long> reservas;
}
