package com.romanowski.pedro.repository;

import com.romanowski.pedro.entity.Filme;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FilmeRepository extends JpaRepository<Filme, Long> {
}
