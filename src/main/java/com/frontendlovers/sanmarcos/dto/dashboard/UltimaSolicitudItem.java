package com.frontendlovers.sanmarcos.dto.dashboard;

import java.time.LocalDate;

public record UltimaSolicitudItem(
        Long id,
        String servicioNombre,
        LocalDate fechaSolicitada,
        String estado,
        String clienteNombre
) {
}
