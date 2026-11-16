package com.frontendlovers.sanmarcos.repository;

import com.frontendlovers.sanmarcos.entity.SolicitudServicio;
import com.frontendlovers.sanmarcos.entity.enums.EstadoSolicitud;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SolicitudServicioRepository extends JpaRepository<SolicitudServicio, Long> {

    List<SolicitudServicio> findByUsuarioId(Long usuarioId);

    List<SolicitudServicio> findByUsuarioIdAndEstado(Long usuarioId, EstadoSolicitud estado);

    List<SolicitudServicio> findByEstado(EstadoSolicitud estado);

    @Query("""
            SELECT s FROM SolicitudServicio s
            LEFT JOIN FETCH s.usuario
            LEFT JOIN FETCH s.servicio
            LEFT JOIN FETCH s.mascota
            WHERE s.id = :id
            """)
    Optional<SolicitudServicio> findByIdWithRelations(@Param("id") Long id);
}
