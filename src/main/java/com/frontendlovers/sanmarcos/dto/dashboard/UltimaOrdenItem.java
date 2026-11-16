package com.frontendlovers.sanmarcos.dto.dashboard;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record UltimaOrdenItem(
        Long id,
        String numeroOrden,
        LocalDateTime fechaOrden,
        BigDecimal total,
        String estado,
        String clienteNombre
) {
}
