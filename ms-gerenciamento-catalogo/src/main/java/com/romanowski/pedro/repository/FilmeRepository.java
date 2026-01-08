package com.romanowski.pedro.repository;

import com.romanowski.pedro.entity.Filme;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FilmeRepository extends JpaRepository<Filme, Long> {

    Optional<Filme> findByTitulo(String titulo);
}
