package com.frontendlovers.sanmarcos.repository;

import com.frontendlovers.sanmarcos.entity.Servicio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ServicioRepository extends JpaRepository<Servicio, Long> {
    List<Servicio> findByActivoTrue();
    Optional<Servicio> findByCodigo(String codigo);
    boolean existsByCodigo(String codigo);
    List<Servicio> findByCategoriaId(Long categoriaId);
}