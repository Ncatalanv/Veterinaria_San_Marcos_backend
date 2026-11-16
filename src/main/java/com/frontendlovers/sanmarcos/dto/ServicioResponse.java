package com.frontendlovers.sanmarcos.dto;

import com.frontendlovers.sanmarcos.entity.Servicio;

import java.math.BigDecimal;

//Devuelve datos públicos de un servicio, pero sin descuento/ stock
public record ServicioResponse(
        Long id,
        String codigo,
        String nombre,
        String descripcion,
        String especie,
        String duracion,
        BigDecimal precio,
        String imagen,
        String nota,
        Long categoriaId,
        String categoriaNombre,
        Boolean activo
) {
    public static ServicioResponse from(Servicio servicio) {
        return new ServicioResponse(
                servicio.getId(),
                servicio.getCodigo(),
                servicio.getNombre(),
                servicio.getDescripcion(),
                servicio.getEspecie(),
                servicio.getDuracion(),
                servicio.getPrecio(),
                servicio.getImagen(),
                servicio.getNota(),
                servicio.getCategoria() != null ? servicio.getCategoria().getId() : null,
                servicio.getCategoria() != null ? servicio.getCategoria().getNombre() : null,
                servicio.getActivo()
        );
    }
}