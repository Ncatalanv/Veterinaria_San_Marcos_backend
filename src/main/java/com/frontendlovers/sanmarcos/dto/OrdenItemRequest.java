package com.frontendlovers.sanmarcos.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

// Línea del carrito: qué producto y cuántas unidades
public record OrdenItemRequest(
        @NotNull Long productoId,
        @NotNull @Min(1) Integer cantidad
) {}
