package com.frontendlovers.sanmarcos.catalogo;

import com.frontendlovers.sanmarcos.entity.enums.TipoCategoria;
import com.frontendlovers.sanmarcos.repository.CategoriaRepository;
import com.frontendlovers.sanmarcos.repository.ProductoRepository;
import com.frontendlovers.sanmarcos.repository.ServicioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class CatalogoPersistenceTest {

    @Autowired CategoriaRepository categoriaRepository;
    @Autowired ProductoRepository productoRepository;
    @Autowired ServicioRepository servicioRepository;

    @Test
    void catalogoSeedCargoCategoriasProductosYServicios() {
        assertThat(categoriaRepository.count()).isGreaterThanOrEqualTo(4);
        assertThat(productoRepository.count()).isGreaterThanOrEqualTo(4);
        assertThat(servicioRepository.count()).isGreaterThanOrEqualTo(4);

        assertThat(categoriaRepository.findByNombreAndTipo("Alimentos", TipoCategoria.PRODUCTO))
                .isPresent();

        assertThat(categoriaRepository.findByTipo(TipoCategoria.SERVICIO))
                .hasSizeGreaterThanOrEqualTo(4);

        assertThat(productoRepository.findByActivoTrueAndDescuentoGreaterThan(0))
                .isNotEmpty();

        assertThat(productoRepository.findByCodigo("ALI-001"))
                .isPresent()
                .hasValueSatisfying(p -> assertThat(p.getStock()).isEqualTo(50));
    }
}