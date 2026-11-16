package com.frontendlovers.sanmarcos.dto;

import com.frontendlovers.sanmarcos.entity.SolicitudServicio;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

// Solicitud de servicio lista para devolver al frontend
public record SolicitudServicioResponse(
        Long id,
        Long usuarioId,
        String usuarioNombre,
        Long servicioId,
        String servicioNombre,
        Long mascotaId,
        String mascotaNombre,
        String especie,
        LocalDate fechaSolicitada,
        LocalDate fechaCita,
        LocalTime horaCita,
        String comentario,
        String estado,
        LocalDateTime createdAt
) {
    public static SolicitudServicioResponse from(SolicitudServicio solicitud) {
        return new SolicitudServicioResponse(
                solicitud.getId(),
                solicitud.getUsuario().getId(),
                solicitud.getUsuario().getNombre() + " " + solicitud.getUsuario().getApellido(),
                solicitud.getServicio().getId(),
                solicitud.getServicio().getNombre(),
                solicitud.getMascota() != null ? solicitud.getMascota().getId() : null,
                solicitud.getMascotaNombre(),
                solicitud.getEspecie(),
                solicitud.getFechaSolicitada(),
                solicitud.getFechaCita(),
                solicitud.getHoraCita(),
                solicitud.getComentario(),
                solicitud.getEstado().name(),
                solicitud.getCreatedAt()
        );
    }
}
