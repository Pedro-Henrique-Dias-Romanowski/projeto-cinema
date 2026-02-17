package com.romanowski.pedro.repository;

import com.romanowski.pedro.entity.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReservaRepository extends JpaRepository<Reserva, Long> {

    List<Reserva> findAllByIdCliente(UUID idCliente);
    Optional<Reserva> findByIdAndIdCliente(Long id, UUID idCliente);

    Long id(Long id);
}
