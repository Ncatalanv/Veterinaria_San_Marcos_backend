package com.frontendlovers.sanmarcos.repository;

import com.frontendlovers.sanmarcos.entity.Producto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductoRepository extends JpaRepository<Producto, Long> {
    List<Producto> findByActivoTrue();
    Optional<Producto> findByCodigo(String codigo);
    boolean existsByCodigo(String codigo);
    List<Producto> findByCategoriaId(Long categoriaId);
    List<Producto> findByActivoTrueAndDescuentoGreaterThan(Integer descuento);
}