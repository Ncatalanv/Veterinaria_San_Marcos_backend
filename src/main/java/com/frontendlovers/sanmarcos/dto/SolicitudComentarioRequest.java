package com.frontendlovers.sanmarcos.dto;

import jakarta.validation.constraints.Size;

// Nuevo comentario de una solicitud de servicio
public record SolicitudComentarioRequest(
        @Size(max = 300) String comentario
) {}
