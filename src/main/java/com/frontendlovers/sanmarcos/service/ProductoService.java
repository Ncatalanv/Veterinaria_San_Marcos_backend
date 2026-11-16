package com.frontendlovers.sanmarcos.service;

import com.frontendlovers.sanmarcos.dto.ProductoResponse;
import com.frontendlovers.sanmarcos.exception.ResourceNotFoundException;
import com.frontendlovers.sanmarcos.repository.ProductoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

//Lista productos aplicando filtros opcionales y busca un producto activo por su ID
@Service
public class ProductoService {

    private final ProductoRepository productoRepository;

    public ProductoService(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    public List<ProductoResponse> listar(Long categoriaId, Boolean soloOfertas, String busqueda) {
        boolean ofertas = soloOfertas != null && soloOfertas;
        String termino = (busqueda != null && !busqueda.isBlank())
                ? "%" + busqueda.toLowerCase() + "%"
                : null;

        return productoRepository.findByFiltros(categoriaId, ofertas, termino)
                .stream()
                .map(ProductoResponse::from)
                .toList();
    }

    public ProductoResponse buscarPorId(Long id) {
        return productoRepository.findByIdWithCategoria(id)
                .filter(p -> Boolean.TRUE.equals(p.getActivo()))
                .map(ProductoResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));
    }
}