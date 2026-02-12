package com.romanowski.pedro.repository;

import com.romanowski.pedro.entity.AdministradorEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdministradorRepository extends JpaRepository<AdministradorEntity, Long> {
}
