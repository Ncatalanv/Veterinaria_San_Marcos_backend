package com.frontendlovers.sanmarcos.repository;

import com.frontendlovers.sanmarcos.entity.Servicio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

//Método JPQL que aplica filtros opcionales --> categoría y busqueda textual 
public interface ServicioRepository extends JpaRepository<Servicio, Long> {
    List<Servicio> findByActivoTrue();
    Optional<Servicio> findByCodigo(String codigo);
    boolean existsByCodigo(String codigo);
    List<Servicio> findByCategoriaId(Long categoriaId);

     @Query("""
            SELECT s FROM Servicio s LEFT JOIN FETCH s.categoria
            WHERE s.activo = true
              AND (:categoriaId IS NULL OR s.categoria.id = :categoriaId)
              AND (:busqueda IS NULL OR LOWER(s.nombre) LIKE :busqueda OR LOWER(s.descripcion) LIKE :busqueda)
            ORDER BY s.nombre
            """)
    List<Servicio> findByFiltros(@Param("categoriaId") Long categoriaId,
                                 @Param("busqueda") String busqueda);

    @Query("SELECT s FROM Servicio s LEFT JOIN FETCH s.categoria WHERE s.id = :id")
    Optional<Servicio> findByIdWithCategoria(@Param("id") Long id);
}