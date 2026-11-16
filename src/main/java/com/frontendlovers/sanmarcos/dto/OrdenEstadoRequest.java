package com.frontendlovers.sanmarcos.dto;

import com.frontendlovers.sanmarcos.entity.enums.EstadoOrden;
import jakarta.validation.constraints.NotNull;

// Body para cambiar el estado de una orden
public record OrdenEstadoRequest(
        @NotNull EstadoOrden estado
) {}
