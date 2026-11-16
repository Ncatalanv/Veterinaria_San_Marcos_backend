package com.frontendlovers.sanmarcos.user.dto;

import com.frontendlovers.sanmarcos.validation.RunChileno;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record UserUpdateRequest(
        @RunChileno
        String run,

        @Email(message = "Debe ser un correo vǭlido")
        String email,

        @Size(min = 8, message = "La contrase��a debe tener al menos 8 caracteres")
        String password,

        String nombre,
        String apellido,

        LocalDate fechaNacimiento,
        String telefono,
        String direccion,
        String region,
        String comuna,

        String rol,

        Boolean activo
) {
}
