package com.frontendlovers.sanmarcos.controller;

import com.frontendlovers.sanmarcos.dto.SolicitudCitaRequest;
import com.frontendlovers.sanmarcos.dto.SolicitudComentarioRequest;
import com.frontendlovers.sanmarcos.dto.SolicitudEstadoRequest;
import com.frontendlovers.sanmarcos.dto.SolicitudServicioRequest;
import com.frontendlovers.sanmarcos.dto.SolicitudServicioResponse;
import com.frontendlovers.sanmarcos.entity.Usuario;
import com.frontendlovers.sanmarcos.exception.ResourceNotFoundException;
import com.frontendlovers.sanmarcos.repository.UsuarioRepository;
import com.frontendlovers.sanmarcos.service.SolicitudServicioService;
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
@RequestMapping("/api/v1/service-requests")
@Tag(name = "Solicitudes de servicio", description = "Gestión de solicitudes de servicios veterinarios")
public class SolicitudServicioController {

    private final SolicitudServicioService solicitudService;
    private final UsuarioRepository usuarioRepository;

    public SolicitudServicioController(SolicitudServicioService solicitudService,
                                       UsuarioRepository usuarioRepository) {
        this.solicitudService = solicitudService;
        this.usuarioRepository = usuarioRepository;
    }

    @PostMapping
    @PreAuthorize("hasRole('CLIENTE')")
    @Operation(summary = "Crear una solicitud de servicio")
    public ResponseEntity<SolicitudServicioResponse> crear(@RequestBody @Valid SolicitudServicioRequest request,
                                                           Authentication authentication) {
        Usuario usuario = usuarioActual(authentication);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(solicitudService.crear(usuario.getId(), request));
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('CLIENTE')")
    @Operation(summary = "Listar mis solicitudes de servicio")
    public ResponseEntity<List<SolicitudServicioResponse>> misSolicitudes(Authentication authentication) {
        Usuario usuario = usuarioActual(authentication);
        return ResponseEntity.ok(solicitudService.listarPorUsuario(usuario.getId()));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','VETERINARIO')")
    @Operation(summary = "Listar todas las solicitudes de servicio")
    public ResponseEntity<List<SolicitudServicioResponse>> listarTodas() {
        return ResponseEntity.ok(solicitudService.listarTodas());
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Ver detalle de una solicitud de servicio")
    public ResponseEntity<SolicitudServicioResponse> obtener(@PathVariable Long id,
                                                             Authentication authentication) {
        SolicitudServicioResponse solicitud = solicitudService.buscarPorId(id);
        if (!esAdminOVeterinario(authentication) && !perteneceAUsuario(solicitud, authentication)) {
            throw new AccessDeniedException("No puedes ver esta solicitud");
        }
        return ResponseEntity.ok(solicitud);
    }

    @PatchMapping("/{id}/date-time")
    @PreAuthorize("hasAnyRole('ADMIN','VETERINARIO')")
    @Operation(summary = "Asignar o cambiar fecha y hora de la cita")
    public ResponseEntity<SolicitudServicioResponse> actualizarCita(@PathVariable Long id,
                                                                    @RequestBody @Valid SolicitudCitaRequest request) {
        return ResponseEntity.ok(solicitudService.actualizarCita(id, request.fechaCita(), request.horaCita()));
    }

    @PatchMapping("/{id}/comment")
    @PreAuthorize("hasAnyRole('ADMIN','VETERINARIO')")
    @Operation(summary = "Actualizar comentario de la solicitud")
    public ResponseEntity<SolicitudServicioResponse> actualizarComentario(@PathVariable Long id,
                                                                          @RequestBody @Valid SolicitudComentarioRequest request) {
        return ResponseEntity.ok(solicitudService.actualizarComentario(id, request.comentario()));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN','VETERINARIO')")
    @Operation(summary = "Cambiar el estado de la solicitud")
    public ResponseEntity<SolicitudServicioResponse> cambiarEstado(@PathVariable Long id,
                                                                   @RequestBody @Valid SolicitudEstadoRequest request) {
        return ResponseEntity.ok(solicitudService.cambiarEstado(id, request.estado()));
    }

    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasRole('CLIENTE')")
    @Operation(summary = "Cancelar mi solicitud")
    public ResponseEntity<SolicitudServicioResponse> cancelar(@PathVariable Long id,
                                                              Authentication authentication) {
        Usuario usuario = usuarioActual(authentication);
        return ResponseEntity.ok(solicitudService.cancelar(id, usuario.getId()));
    }

    private Usuario usuarioActual(Authentication authentication) {
        return usuarioRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
    }

    private boolean esAdminOVeterinario(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_VETERINARIO"));
    }

    private boolean perteneceAUsuario(SolicitudServicioResponse solicitud, Authentication authentication) {
        Usuario usuario = usuarioActual(authentication);
        return solicitud.usuarioId().equals(usuario.getId());
    }
}
