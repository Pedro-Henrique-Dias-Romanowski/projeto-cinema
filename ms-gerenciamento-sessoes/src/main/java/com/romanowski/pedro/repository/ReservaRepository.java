package com.romanowski.pedro.repository;

import com.romanowski.pedro.entity.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReservaRepository extends JpaRepository<Reserva, Long> {

    List<Reserva> findAllByIdCliente(Long idCliente);
    Optional<Reserva> findByIdAndIdCliente(Long id, Long idCliente);

    Long id(Long id);
}
