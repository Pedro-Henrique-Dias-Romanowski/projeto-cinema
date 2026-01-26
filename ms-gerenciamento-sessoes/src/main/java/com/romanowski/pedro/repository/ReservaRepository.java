package com.romanowski.pedro.repository;

import com.romanowski.pedro.entity.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReservaRepository extends JpaRepository<Reserva, Long> {

    List<Reserva> findAllByIdCliente(Long idCliente);
}
