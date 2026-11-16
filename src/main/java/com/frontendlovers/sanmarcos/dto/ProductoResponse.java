package com.frontendlovers.sanmarcos.dto;

import com.frontendlovers.sanmarcos.entity.Producto;

import java.math.BigDecimal;
import java.math.RoundingMode;

//Devuelve datos públicos de un producto, incluyendo el precio final calculado a partir del descuento
public record ProductoResponse(
        Long id,
        String codigo,
        String nombre,
        String descripcion,
        String presentacion,
        BigDecimal precio,
        Integer descuento,
        BigDecimal precioFinal,
        Integer stock,
        Integer stockCritico,
        String imagen,
        Long categoriaId,
        String categoriaNombre,
        Boolean activo
) {
    public static ProductoResponse from(Producto producto) {
        return new ProductoResponse(
                producto.getId(),
                producto.getCodigo(),
                producto.getNombre(),
                producto.getDescripcion(),
                producto.getPresentacion(),
                producto.getPrecio(),
                producto.getDescuento(),
                calcularPrecioFinal(producto.getPrecio(), producto.getDescuento()),
                producto.getStock(),
                producto.getStockCritico(),
                producto.getImagen(),
                producto.getCategoria() != null ? producto.getCategoria().getId() : null,
                producto.getCategoria() != null ? producto.getCategoria().getNombre() : null,
                producto.getActivo()
        );
    }

    private static BigDecimal calcularPrecioFinal(BigDecimal precio, Integer descuento) {
        if (precio == null || descuento == null || descuento == 0) {
            return precio;
        }
        BigDecimal factor = BigDecimal.valueOf(100 - descuento)
                .divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
        return precio.multiply(factor).setScale(2, RoundingMode.HALF_UP);
    }
}