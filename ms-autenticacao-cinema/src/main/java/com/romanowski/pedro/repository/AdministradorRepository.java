package com.romanowski.pedro.repository;

import com.romanowski.pedro.entity.AdministradorEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AdministradorRepository extends JpaRepository<AdministradorEntity, UUID> {

    Optional<AdministradorEntity> findByEmailIgnoreCase(String email);
}
