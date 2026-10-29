package com.frontendlovers.sanmarcos.repository;

import com.frontendlovers.sanmarcos.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByRun(String run);
    Optional<Usuario> findByEmail(String email);
    boolean existsByRun(String run);
    boolean existsByEmail(String email);
}