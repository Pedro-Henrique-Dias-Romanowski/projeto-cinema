package com.romanowski.pedro.repository;

import com.romanowski.pedro.entity.ClienteEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ClienteRepository extends JpaRepository<ClienteEntity, UUID> {

    Boolean existsByEmail(String email);
    Optional<ClienteEntity> findByEmailIgnoreCase(String email);
}
