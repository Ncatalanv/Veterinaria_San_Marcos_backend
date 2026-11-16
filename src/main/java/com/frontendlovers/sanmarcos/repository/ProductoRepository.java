package com.frontendlovers.sanmarcos.repository;

import com.frontendlovers.sanmarcos.entity.Producto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

//Método JPQL que aplica tres filtros opcionales --> categoría, ofertas y busqueda textual en nombre/ descripción
public interface ProductoRepository extends JpaRepository<Producto, Long> {
    List<Producto> findByActivoTrue();
    Optional<Producto> findByCodigo(String codigo);
    boolean existsByCodigo(String codigo);
    List<Producto> findByCategoriaId(Long categoriaId);
    List<Producto> findByActivoTrueAndDescuentoGreaterThan(Integer descuento);

     @Query("""
            SELECT p FROM Producto p LEFT JOIN FETCH p.categoria
            WHERE p.activo = true
              AND (:categoriaId IS NULL OR p.categoria.id = :categoriaId)
              AND (:soloOfertas = false OR p.descuento > 0)
              AND (:busqueda IS NULL OR LOWER(p.nombre) LIKE :busqueda OR LOWER(p.descripcion) LIKE :busqueda)
            ORDER BY p.nombre
            """)
    List<Producto> findByFiltros(@Param("categoriaId") Long categoriaId,
                                 @Param("soloOfertas") boolean soloOfertas,
                                 @Param("busqueda") String busqueda);

    @Query("SELECT p FROM Producto p LEFT JOIN FETCH p.categoria WHERE p.id = :id")
    Optional<Producto> findByIdWithCategoria(@Param("id") Long id);
    Optional<Producto> findTopByOrderByIdDesc();
}