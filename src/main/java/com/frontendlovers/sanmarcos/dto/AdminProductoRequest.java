package com.frontendlovers.sanmarcos.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

//DTO para crear y actualizar productos con validaciones
public record AdminProductoRequest(
        @NotBlank @Size(max = 120) String nombre,
        String descripcion,
        String presentacion,
        @NotNull @Positive BigDecimal precio,
        @NotNull @Min(0) @Max(100) Integer descuento,
        @NotNull @Min(0) Integer stock,
        @NotNull @Min(0) Integer stockCritico,
        String imagen,
        @NotNull Long categoriaId
) {}