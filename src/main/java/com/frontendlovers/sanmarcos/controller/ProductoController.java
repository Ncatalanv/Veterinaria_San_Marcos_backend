package com.frontendlovers.sanmarcos.controller;

import com.frontendlovers.sanmarcos.dto.ProductoResponse;
import com.frontendlovers.sanmarcos.service.ProductoService;
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
@RequestMapping("/api/v1/products")
@Tag(name = "Productos", description = "Catálogo público de productos")
public class ProductoController {

    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    @GetMapping
    @Operation(summary = "Listar productos", description = "Filtra por categoría, ofertas y búsqueda textual")
    public ResponseEntity<List<ProductoResponse>> listar(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Boolean onSale,
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(productoService.listar(categoryId, onSale, search));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener producto por ID")
    public ResponseEntity<ProductoResponse> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(productoService.buscarPorId(id));
    }
}