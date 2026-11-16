package com.frontendlovers.sanmarcos.dto;

import com.frontendlovers.sanmarcos.entity.enums.EstadoSolicitud;
import jakarta.validation.constraints.NotNull;

// Nuevo estado de una solicitud de servicio
public record SolicitudEstadoRequest(
        @NotNull EstadoSolicitud estado
) {}
