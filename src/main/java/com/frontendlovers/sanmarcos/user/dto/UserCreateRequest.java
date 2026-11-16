package com.frontendlovers.sanmarcos.user.dto;

import com.frontendlovers.sanmarcos.validation.RunChileno;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record UserCreateRequest(
        @NotBlank(message = "El RUN es obligatorio")
        @RunChileno
        String run,

        @NotBlank(message = "El email es obligatorio")
        @Email(message = "Debe ser un correo vǭlido")
        String email,

        @NotBlank(message = "La contrase��a es obligatoria")
        @Size(min = 8, message = "La contrase��a debe tener al menos 8 caracteres")
        String password,

        @NotBlank(message = "El nombre es obligatorio")
        String nombre,

        @NotBlank(message = "El apellido es obligatorio")
        String apellido,

        LocalDate fechaNacimiento,
        String telefono,
        String direccion,
        String region,
        String comuna,

        @NotBlank(message = "El rol es obligatorio")
        String rol,

        Boolean activo
) {
}
