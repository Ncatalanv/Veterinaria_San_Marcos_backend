package com.frontendlovers.sanmarcos.repository;

import com.frontendlovers.sanmarcos.entity.Orden;
import com.frontendlovers.sanmarcos.entity.Usuario;
import com.frontendlovers.sanmarcos.entity.enums.EstadoOrden;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

//Repositorio de órdenes con búsquedas por número, usuario y estado
public interface OrdenRepository extends JpaRepository<Orden, Long> {

    Optional<Orden> findByNumeroOrden(String numeroOrden);

    List<Orden> findByUsuarioId(Long usuarioId);

    List<Orden> findByUsuario(Usuario usuario);

    List<Orden> findByEstado(EstadoOrden estado);

    boolean existsByNumeroOrden(String numeroOrden);

    @Query("SELECT o FROM Orden o LEFT JOIN FETCH o.detalles WHERE o.id = :id")
    Optional<Orden> findByIdWithDetalles(@Param("id") Long id);
}