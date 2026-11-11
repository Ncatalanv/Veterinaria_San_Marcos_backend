package com.frontendlovers.sanmarcos.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MascotaRequest(
        @NotBlank @Size(max = 80) String nombre,
        @NotBlank @Size(max = 50) String especie,
        @Size(max = 80) String raza
) {
}
