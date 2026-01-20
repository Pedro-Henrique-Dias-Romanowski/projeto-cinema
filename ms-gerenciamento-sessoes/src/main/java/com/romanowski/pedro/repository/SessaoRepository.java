package com.romanowski.pedro.repository;

import com.romanowski.pedro.entity.Sessao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface SessaoRepository extends JpaRepository<Sessao, Long> {

    Boolean existsBySalaAndDataHoraSessao(Integer sala, LocalDateTime dataHoraSessao);
}
