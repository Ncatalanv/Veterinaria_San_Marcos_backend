package com.frontendlovers.sanmarcos.controller;

import com.frontendlovers.sanmarcos.dto.OrdenEstadoRequest;
import com.frontendlovers.sanmarcos.dto.OrdenRequest;
import com.frontendlovers.sanmarcos.dto.OrdenResponse;
import com.frontendlovers.sanmarcos.entity.Usuario;
import com.frontendlovers.sanmarcos.exception.ResourceNotFoundException;
import com.frontendlovers.sanmarcos.repository.UsuarioRepository;
import com.frontendlovers.sanmarcos.service.OrdenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
@Tag(name = "Órdenes", description = "Gestión de órdenes de productos")
public class OrdenController {

    private final OrdenService ordenService;
    private final UsuarioRepository usuarioRepository;

    public OrdenController(OrdenService ordenService,
                           UsuarioRepository usuarioRepository) {
        this.ordenService = ordenService;
        this.usuarioRepository = usuarioRepository;
    }

    @PostMapping
    @PreAuthorize("hasRole('CLIENTE')")
    @Operation(summary = "Crear una orden desde el carrito")
    public ResponseEntity<OrdenResponse> crear(@RequestBody @Valid OrdenRequest request,
                                               Authentication authentication) {
        Usuario usuario = usuarioActual(authentication);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ordenService.crearOrden(usuario.getId(), request));
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('CLIENTE')")
    @Operation(summary = "Listar mis órdenes")
    public ResponseEntity<List<OrdenResponse>> misOrdenes(Authentication authentication) {
        Usuario usuario = usuarioActual(authentication);
        return ResponseEntity.ok(ordenService.listarPorUsuario(usuario.getId()));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','VENDEDOR')")
    @Operation(summary = "Listar todas las órdenes")
    public ResponseEntity<List<OrdenResponse>> listarTodas() {
        return ResponseEntity.ok(ordenService.listarTodas());
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Ver detalle de una orden")
    public ResponseEntity<OrdenResponse> obtener(@PathVariable Long id,
                                                 Authentication authentication) {
        OrdenResponse orden = ordenService.buscarPorId(id);
        if (!esAdminOVendedor(authentication) && !perteneceAUsuario(orden, authentication)) {
            throw new AccessDeniedException("No puedes ver esta orden");
        }
        return ResponseEntity.ok(orden);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Cambiar el estado de una orden")
    public ResponseEntity<OrdenResponse> cambiarEstado(@PathVariable Long id,
                                                       @RequestBody @Valid OrdenEstadoRequest request) {
        return ResponseEntity.ok(ordenService.cambiarEstado(id, request.estado()));
    }

    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasRole('CLIENTE')")
    @Operation(summary = "Cancelar mi orden")
    public ResponseEntity<OrdenResponse> cancelar(@PathVariable Long id,
                                                  Authentication authentication) {
        Usuario usuario = usuarioActual(authentication);
        return ResponseEntity.ok(ordenService.cancelarOrden(id, usuario.getId()));
    }

    private Usuario usuarioActual(Authentication authentication) {
        return usuarioRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
    }

    private boolean esAdminOVendedor(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_VENDEDOR"));
    }

    private boolean perteneceAUsuario(OrdenResponse orden, Authentication authentication) {
        Usuario usuario = usuarioActual(authentication);
        return orden.usuarioId().equals(usuario.getId());
    }
}
