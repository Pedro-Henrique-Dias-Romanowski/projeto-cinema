package com.romanowski.pedro.repository;

import com.romanowski.pedro.entity.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ClienteRepository extends JpaRepository<Cliente, UUID> {

    Boolean existsByEmail(String email);
    Optional<Cliente> findById(UUID id);
}
