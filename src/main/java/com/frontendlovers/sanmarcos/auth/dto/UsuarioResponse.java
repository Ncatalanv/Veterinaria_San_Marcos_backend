package com.frontendlovers.sanmarcos.auth.dto;

import com.frontendlovers.sanmarcos.entity.Usuario;

import java.time.LocalDate;

public record UsuarioResponse(
        Long id,
        String run,
        String email,
        String nombre,
        String apellido,
        LocalDate fechaNacimiento,
        String telefono,
        String direccion,
        String region,
        String comuna,
        String rol
) {

    public static UsuarioResponse from(Usuario usuario) {
        return new UsuarioResponse(
                usuario.getId(),
                usuario.getRun(),
                usuario.getEmail(),
                usuario.getNombre(),
                usuario.getApellido(),
                usuario.getFechaNacimiento(),
                usuario.getTelefono(),
                usuario.getDireccion(),
                usuario.getRegion(),
                usuario.getComuna(),
                usuario.getRol() != null ? usuario.getRol().getNombre() : null
        );
    }
}
