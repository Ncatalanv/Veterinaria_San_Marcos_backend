package com.frontendlovers.sanmarcos.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

// Datos para crear una solicitud de servicio veterinario
public record SolicitudServicioRequest(
        @NotNull Long servicioId,
        Long mascotaId,
        @NotBlank @Size(max = 80) String mascotaNombre,
        @NotBlank @Size(max = 50) String especie,
        LocalDate fechaSolicitada,
        @Size(max = 300) String comentario
) {}
