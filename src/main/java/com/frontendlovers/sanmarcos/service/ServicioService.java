package com.frontendlovers.sanmarcos.service;

import com.frontendlovers.sanmarcos.dto.ServicioResponse;
import com.frontendlovers.sanmarcos.exception.ResourceNotFoundException;
import com.frontendlovers.sanmarcos.repository.ServicioRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ServicioService {

    private final ServicioRepository servicioRepository;

    public ServicioService(ServicioRepository servicioRepository) {
        this.servicioRepository = servicioRepository;
    }

    public List<ServicioResponse> listar(Long categoriaId, String busqueda) {
        String termino = (busqueda != null && !busqueda.isBlank())
                ? "%" + busqueda.toLowerCase() + "%"
                : null;

        return servicioRepository.findByFiltros(categoriaId, termino)
                .stream()
                .map(ServicioResponse::from)
                .toList();
    }

    public ServicioResponse buscarPorId(Long id) {
        return servicioRepository.findByIdWithCategoria(id)
                .filter(s -> Boolean.TRUE.equals(s.getActivo()))
                .map(ServicioResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Servicio no encontrado"));
    }
}