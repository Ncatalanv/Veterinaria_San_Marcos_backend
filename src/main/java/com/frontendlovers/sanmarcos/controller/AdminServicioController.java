package com.frontendlovers.sanmarcos.controller;

import com.frontendlovers.sanmarcos.dto.AdminServicioRequest;
import com.frontendlovers.sanmarcos.dto.ServicioResponse;
import com.frontendlovers.sanmarcos.service.AdminServicioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/services")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Servicios", description = "Gestión de servicios (solo ADMIN)")
public class AdminServicioController {

    private final AdminServicioService adminServicioService;

    public AdminServicioController(AdminServicioService adminServicioService) {
        this.adminServicioService = adminServicioService;
    }

    @GetMapping
    @Operation(summary = "Listar todos los servicios")
    public ResponseEntity<List<ServicioResponse>> listar() {
        return ResponseEntity.ok(adminServicioService.listar());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar servicio por ID")
    public ResponseEntity<ServicioResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(adminServicioService.buscarPorId(id));
    }

    @PostMapping
    @Operation(summary = "Crear servicio")
    public ResponseEntity<ServicioResponse> crear(@RequestBody @Valid AdminServicioRequest request) {
        return ResponseEntity.ok(adminServicioService.crear(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar servicio")
    public ResponseEntity<ServicioResponse> actualizar(@PathVariable Long id,
                                                       @RequestBody @Valid AdminServicioRequest request) {
        return ResponseEntity.ok(adminServicioService.actualizar(id, request));
    }

    @PatchMapping("/{id}/toggle")
    @Operation(summary = "Activar/desactivar servicio")
    public ResponseEntity<ServicioResponse> toggle(@PathVariable Long id) {
        return ResponseEntity.ok(adminServicioService.toggle(id));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar servicio")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        adminServicioService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}