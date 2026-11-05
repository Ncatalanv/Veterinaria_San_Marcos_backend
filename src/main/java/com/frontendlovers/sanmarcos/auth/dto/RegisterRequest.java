package com.frontendlovers.sanmarcos.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record RegisterRequest(
        @NotBlank @Size(max = 15) String run,
        @NotBlank @Email @Size(max = 120) String email,
        @NotBlank @Size(min = 8, max = 72) String password,
        @NotBlank @Size(max = 80) String nombre,
        @NotBlank @Size(max = 80) String apellido,
        @Past LocalDate fechaNacimiento,
        @Size(max = 20) String telefono,
        @Size(max = 255) String direccion,
        @Size(max = 80) String region,
        @Size(max = 80) String comuna
) {
}
