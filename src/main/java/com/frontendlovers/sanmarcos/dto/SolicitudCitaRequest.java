package com.frontendlovers.sanmarcos.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

// Datos para asignar o modificar fecha y hora de una cita
public record SolicitudCitaRequest(
        @NotNull LocalDate fechaCita,
        @NotNull LocalTime horaCita
) {}
