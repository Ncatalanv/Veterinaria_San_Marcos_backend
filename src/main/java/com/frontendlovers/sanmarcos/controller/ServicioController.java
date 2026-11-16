package com.frontendlovers.sanmarcos.controller;

import com.frontendlovers.sanmarcos.dto.ServicioResponse;
import com.frontendlovers.sanmarcos.service.ServicioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/services")
@Tag(name = "Servicios", description = "Catálogo público de servicios")
public class ServicioController {

    private final ServicioService servicioService;

    public ServicioController(ServicioService servicioService) {
        this.servicioService = servicioService;
    }

    @GetMapping
    @Operation(summary = "Listar servicios", description = "Filtra por categoría y búsqueda textual")
    public ResponseEntity<List<ServicioResponse>> listar(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(servicioService.listar(categoryId, search));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener servicio por ID")
    public ResponseEntity<ServicioResponse> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(servicioService.buscarPorId(id));
    }
}