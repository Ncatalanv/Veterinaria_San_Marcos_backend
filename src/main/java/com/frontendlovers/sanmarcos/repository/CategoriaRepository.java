package com.frontendlovers.sanmarcos.repository;

import com.frontendlovers.sanmarcos.entity.Categoria;
import com.frontendlovers.sanmarcos.entity.enums.TipoCategoria;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
    List<Categoria> findByTipo(TipoCategoria tipo);
    Optional<Categoria> findByNombreAndTipo(String nombre, TipoCategoria tipo);
}