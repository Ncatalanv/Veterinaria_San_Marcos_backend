package com.frontendlovers.sanmarcos.controller;

import com.frontendlovers.sanmarcos.dto.AdminProductoRequest;
import com.frontendlovers.sanmarcos.dto.ProductoResponse;
import com.frontendlovers.sanmarcos.service.AdminProductoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

//Endpoints REST
@RestController
@RequestMapping("/api/v1/admin/products")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Productos", description = "Gestión de productos (solo ADMIN)")
public class AdminProductoController {

    private final AdminProductoService adminProductoService;

    public AdminProductoController(AdminProductoService adminProductoService) {
        this.adminProductoService = adminProductoService;
    }

    @GetMapping
    @Operation(summary = "Listar todos los productos")
    public ResponseEntity<List<ProductoResponse>> listar() {
        return ResponseEntity.ok(adminProductoService.listar());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar producto por ID")
    public ResponseEntity<ProductoResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(adminProductoService.buscarPorId(id));
    }

    @PostMapping
    @Operation(summary = "Crear producto")
    public ResponseEntity<ProductoResponse> crear(@RequestBody @Valid AdminProductoRequest request) {
        return ResponseEntity.ok(adminProductoService.crear(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar producto")
    public ResponseEntity<ProductoResponse> actualizar(@PathVariable Long id,
                                                       @RequestBody @Valid AdminProductoRequest request) {
        return ResponseEntity.ok(adminProductoService.actualizar(id, request));
    }

    @PatchMapping("/{id}/toggle")
    @Operation(summary = "Activar/desactivar producto")
    public ResponseEntity<ProductoResponse> toggle(@PathVariable Long id) {
        return ResponseEntity.ok(adminProductoService.toggle(id));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar producto")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        adminProductoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}