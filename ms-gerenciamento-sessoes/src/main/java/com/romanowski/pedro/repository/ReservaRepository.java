package com.romanowski.pedro.repository;

import com.romanowski.pedro.entity.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservaRepository extends JpaRepository<Reserva, Long> {
}
