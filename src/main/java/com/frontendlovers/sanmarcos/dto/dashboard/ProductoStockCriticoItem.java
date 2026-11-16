package com.frontendlovers.sanmarcos.dto.dashboard;

public record ProductoStockCriticoItem(
        Long id,
        String codigo,
        String nombre,
        Integer stock,
        Integer stockCritico
) {
}
