package com.frontendlovers.sanmarcos.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

//DTO para crear y actualizar servicios con validaciones
public record AdminServicioRequest(
        @NotBlank @Size(max = 120) String nombre,
        String descripcion,
        @Size(max = 80) String especie,
        @Size(max = 40) String duracion,
        @NotNull @Positive BigDecimal precio,
        String imagen,
        @Size(max = 255) String nota,
        @NotNull Long categoriaId
) {}