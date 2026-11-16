package com.frontendlovers.sanmarcos.service;

import com.frontendlovers.sanmarcos.dto.AdminServicioRequest;
import com.frontendlovers.sanmarcos.dto.ServicioResponse;
import com.frontendlovers.sanmarcos.entity.Categoria;
import com.frontendlovers.sanmarcos.entity.Servicio;
import com.frontendlovers.sanmarcos.entity.enums.TipoCategoria;
import com.frontendlovers.sanmarcos.exception.ResourceNotFoundException;
import com.frontendlovers.sanmarcos.repository.CategoriaRepository;
import com.frontendlovers.sanmarcos.repository.ServicioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AdminServicioService {

    private final ServicioRepository servicioRepository;
    private final CategoriaRepository categoriaRepository;

    public AdminServicioService(ServicioRepository servicioRepository,
                                CategoriaRepository categoriaRepository) {
        this.servicioRepository = servicioRepository;
        this.categoriaRepository = categoriaRepository;
    }

    @Transactional(readOnly = true)
    public List<ServicioResponse> listar() {
        return servicioRepository.findAll()
                .stream()
                .map(ServicioResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public ServicioResponse buscarPorId(Long id) {
        return servicioRepository.findByIdWithCategoria(id)
                .map(ServicioResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Servicio no encontrado"));
    }

    @Transactional
    public ServicioResponse crear(AdminServicioRequest request) {
        Categoria categoria = categoriaServicio(request.categoriaId());
        Servicio servicio = Servicio.builder()
                .codigo(generarCodigoServicio())
                .nombre(request.nombre())
                .descripcion(request.descripcion())
                .especie(request.especie())
                .duracion(request.duracion())
                .precio(request.precio())
                .imagen(request.imagen())
                .nota(request.nota())
                .categoria(categoria)
                .activo(true)
                .build();
        return ServicioResponse.from(servicioRepository.save(servicio));
    }

    @Transactional
    public ServicioResponse actualizar(Long id, AdminServicioRequest request) {
        Servicio servicio = servicioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Servicio no encontrado"));
        Categoria categoria = categoriaServicio(request.categoriaId());
        servicio.setNombre(request.nombre());
        servicio.setDescripcion(request.descripcion());
        servicio.setEspecie(request.especie());
        servicio.setDuracion(request.duracion());
        servicio.setPrecio(request.precio());
        servicio.setImagen(request.imagen());
        servicio.setNota(request.nota());
        servicio.setCategoria(categoria);
        return ServicioResponse.from(servicioRepository.save(servicio));
    }

    @Transactional
    public ServicioResponse toggle(Long id) {
        Servicio servicio = servicioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Servicio no encontrado"));
        servicio.setActivo(!servicio.getActivo());
        return ServicioResponse.from(servicioRepository.save(servicio));
    }

    @Transactional
    public void eliminar(Long id) {
        Servicio servicio = servicioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Servicio no encontrado"));
        servicioRepository.delete(servicio);
    }

    private Categoria categoriaServicio(Long categoriaId) {
        Categoria categoria = categoriaRepository.findById(categoriaId)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada"));
        if (categoria.getTipo() != TipoCategoria.SERVICIO) {
            throw new ResourceNotFoundException("La categoría no es de tipo SERVICIO");
        }
        return categoria;
    }

    private String generarCodigoServicio() {
        long siguiente = servicioRepository.findTopByOrderByIdDesc()
                .map(s -> s.getId() + 1)
                .orElse(1L);
        return String.format("SERV-%04d", siguiente);
    }
}