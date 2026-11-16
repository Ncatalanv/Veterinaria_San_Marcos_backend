package com.frontendlovers.sanmarcos.dto;

import com.frontendlovers.sanmarcos.entity.OrdenDetalle;

import java.math.BigDecimal;

// Línea de detalle de una orden, con precio con descuento y subtotal
public record OrdenDetalleResponse(
        Long id,
        Long productoId,
        String productoNombre,
        BigDecimal precioUnitario,
        Integer descuento,
        Integer cantidad,
        BigDecimal subtotal
) {
    public static OrdenDetalleResponse from(OrdenDetalle detalle) {
        return new OrdenDetalleResponse(
                detalle.getId(),
                detalle.getProducto() != null ? detalle.getProducto().getId() : null,
                detalle.getProductoNombre(),
                detalle.getPrecioUnitario(),
                detalle.getDescuento(),
                detalle.getCantidad(),
                detalle.getSubtotal()
        );
    }
}
