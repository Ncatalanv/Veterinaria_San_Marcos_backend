package com.frontendlovers.sanmarcos.controller;

import com.frontendlovers.sanmarcos.dto.CategoriaResponse;
import com.frontendlovers.sanmarcos.service.CategoriaService;
import com.frontendlovers.sanmarcos.entity.enums.TipoCategoria;
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
@RequestMapping("/api/v1/categories")
@Tag(name = "Categorías", description = "Catálogo público de categorías")
public class CategoriaController {

    private final CategoriaService categoriaService;

    public CategoriaController(CategoriaService categoriaService) {
        this.categoriaService = categoriaService;
    }

    @GetMapping
    @Operation(summary = "Listar categorías", description = "Lista todas las categorías o filtra por tipo (PRODUCTO/SERVICIO)")
    public ResponseEntity<List<CategoriaResponse>> listar(
            @RequestParam(required = false) TipoCategoria type) {
        return ResponseEntity.ok(categoriaService.listar(type));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener categoría por ID")
    public ResponseEntity<CategoriaResponse> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(categoriaService.buscarPorId(id));
    }
}