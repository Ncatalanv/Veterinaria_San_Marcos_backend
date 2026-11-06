package com.frontendlovers.sanmarcos.auth.controller;

import com.frontendlovers.sanmarcos.auth.dto.AuthResponse;
import com.frontendlovers.sanmarcos.auth.dto.LoginRequest;
import com.frontendlovers.sanmarcos.auth.dto.RegisterRequest;
import com.frontendlovers.sanmarcos.auth.dto.UsuarioResponse;
import com.frontendlovers.sanmarcos.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Autenticación", description = "Registro, login y perfil de usuario")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @Operation(summary = "Registrar un nuevo cliente", description = "Crea un usuario con rol CLIENTE y devuelve un JWT")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión", description = "Autentica un usuario y devuelve un JWT")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @GetMapping("/me")
    @Operation(summary = "Obtener perfil autenticado", description = "Devuelve los datos del usuario del token JWT")
    public ResponseEntity<UsuarioResponse> me() {
        return ResponseEntity.ok(authService.me());
    }
}
