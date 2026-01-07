package com.romanowski.pedro.repository;

import com.romanowski.pedro.entity.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    Boolean existsByEmail(String email);
    Optional<Cliente> findById(Long id);
}
