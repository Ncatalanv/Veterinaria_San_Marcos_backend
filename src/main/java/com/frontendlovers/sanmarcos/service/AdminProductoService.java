package com.frontendlovers.sanmarcos.service;

import com.frontendlovers.sanmarcos.dto.AdminProductoRequest;
import com.frontendlovers.sanmarcos.dto.ProductoResponse;
import com.frontendlovers.sanmarcos.entity.Categoria;
import com.frontendlovers.sanmarcos.entity.Producto;
import com.frontendlovers.sanmarcos.entity.enums.TipoCategoria;
import com.frontendlovers.sanmarcos.exception.ResourceNotFoundException;
import com.frontendlovers.sanmarcos.repository.CategoriaRepository;
import com.frontendlovers.sanmarcos.repository.ProductoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

//Lógica de negocio del CRUD de productos: valida cateoría del tipo correcto, genera códigom actualiza, activa/ desactiva y elimina
@Service
public class AdminProductoService {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;

    public AdminProductoService(ProductoRepository productoRepository,
                                CategoriaRepository categoriaRepository) {
        this.productoRepository = productoRepository;
        this.categoriaRepository = categoriaRepository;
    }

    @Transactional(readOnly = true)
    public List<ProductoResponse> listar() {
        return productoRepository.findAll()
                .stream()
                .map(ProductoResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProductoResponse buscarPorId(Long id) {
        return productoRepository.findByIdWithCategoria(id)
                .map(ProductoResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));
    }

    @Transactional
    public ProductoResponse crear(AdminProductoRequest request) {
        Categoria categoria = categoriaProducto(request.categoriaId());
        Producto producto = Producto.builder()
                .codigo(generarCodigoProducto())
                .nombre(request.nombre())
                .descripcion(request.descripcion())
                .presentacion(request.presentacion())
                .precio(request.precio())
                .descuento(request.descuento())
                .stock(request.stock())
                .stockCritico(request.stockCritico())
                .imagen(request.imagen())
                .categoria(categoria)
                .activo(true)
                .build();
        return ProductoResponse.from(productoRepository.save(producto));
    }

    @Transactional
    public ProductoResponse actualizar(Long id, AdminProductoRequest request) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));
        Categoria categoria = categoriaProducto(request.categoriaId());
        producto.setNombre(request.nombre());
        producto.setDescripcion(request.descripcion());
        producto.setPresentacion(request.presentacion());
        producto.setPrecio(request.precio());
        producto.setDescuento(request.descuento());
        producto.setStock(request.stock());
        producto.setStockCritico(request.stockCritico());
        producto.setImagen(request.imagen());
        producto.setCategoria(categoria);
        return ProductoResponse.from(productoRepository.save(producto));
    }

    @Transactional
    public ProductoResponse toggle(Long id) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));
        producto.setActivo(!producto.getActivo());
        return ProductoResponse.from(productoRepository.save(producto));
    }

    @Transactional
    public void eliminar(Long id) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));
        productoRepository.delete(producto);
    }

    private Categoria categoriaProducto(Long categoriaId) {
        Categoria categoria = categoriaRepository.findById(categoriaId)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada"));
        if (categoria.getTipo() != TipoCategoria.PRODUCTO) {
            throw new ResourceNotFoundException("La categoría no es de tipo PRODUCTO");
        }
        return categoria;
    }

    private String generarCodigoProducto() {
        long siguiente = productoRepository.findTopByOrderByIdDesc()
                .map(p -> p.getId() + 1)
                .orElse(1L);
        return String.format("PROD-%04d", siguiente);
    }
}