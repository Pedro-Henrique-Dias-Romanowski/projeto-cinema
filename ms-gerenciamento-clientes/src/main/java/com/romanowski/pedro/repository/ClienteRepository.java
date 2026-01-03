package com.romanowski.pedro.repository;

import com.romanowski.pedro.entity.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    Boolean existsByEmail(String email);
}
