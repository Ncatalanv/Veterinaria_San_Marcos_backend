package com.frontendlovers.sanmarcos.auth.dto;

public record AuthResponse(
        String token,
        String tokenType,
        long expiresIn,
        UsuarioResponse usuario
) {
}
