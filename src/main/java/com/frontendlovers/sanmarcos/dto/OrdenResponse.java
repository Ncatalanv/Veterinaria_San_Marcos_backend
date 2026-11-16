package com.frontendlovers.sanmarcos.dto;

import com.frontendlovers.sanmarcos.entity.Orden;
import com.frontendlovers.sanmarcos.entity.enums.TipoEntrega;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

// Orden completa lista para devolver al frontend
public record OrdenResponse(
        Long id,
        String numeroOrden,
        String estado,
        BigDecimal total,
        TipoEntrega tipoEntrega,
        String direccionEnvio,
        String instrucciones,
        String contactoNombre,
        String contactoApellido,
        String contactoEmail,
        String contactoTelefono,
        LocalDateTime createdAt,
        List<OrdenDetalleResponse> detalles
) {
    public static OrdenResponse from(Orden orden) {
        return new OrdenResponse(
                orden.getId(),
                orden.getNumeroOrden(),
                orden.getEstado().name(),
                orden.getTotal(),
                orden.getTipoEntrega(),
                orden.getDireccionEnvio(),
                orden.getInstrucciones(),
                orden.getContactoNombre(),
                orden.getContactoApellido(),
                orden.getContactoEmail(),
                orden.getContactoTelefono(),
                orden.getCreatedAt(),
                orden.getDetalles().stream()
                        .map(OrdenDetalleResponse::from)
                        .toList()
        );
    }
}
