package com.frontendlovers.sanmarcos.service;

import com.frontendlovers.sanmarcos.dto.CategoriaResponse;
import com.frontendlovers.sanmarcos.entity.enums.TipoCategoria;
import com.frontendlovers.sanmarcos.exception.ResourceNotFoundException;
import com.frontendlovers.sanmarcos.repository.CategoriaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

//Expone categorías. Si llega un tipo Producto/ Servicio, filtra. Sino, devuelve todas
@Service
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;

    public CategoriaService(CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    public List<CategoriaResponse> listar(TipoCategoria tipo) {
        return (tipo != null ? categoriaRepository.findByTipo(tipo)
                             : categoriaRepository.findAll())
                .stream()
                .map(CategoriaResponse::from)
                .toList();
    }

    public CategoriaResponse buscarPorId(Long id) {
        return categoriaRepository.findById(id)
                .map(CategoriaResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada"));
    }
}