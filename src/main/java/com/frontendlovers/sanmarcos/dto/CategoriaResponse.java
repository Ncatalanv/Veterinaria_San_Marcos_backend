package com.frontendlovers.sanmarcos.dto;

import com.frontendlovers.sanmarcos.entity.Categoria;
import com.frontendlovers.sanmarcos.entity.enums.TipoCategoria;

//Transformación de una Categoria en u nobjeto plano para enviar al frontend, ocultando listas de productos/ servicios
public record CategoriaResponse(
        Long id,
        String nombre,
        TipoCategoria tipo,
        String descripcion
) {
    public static CategoriaResponse from(Categoria categoria) {
        return new CategoriaResponse(
                categoria.getId(),
                categoria.getNombre(),
                categoria.getTipo(),
                categoria.getDescripcion()
        );
    }
}