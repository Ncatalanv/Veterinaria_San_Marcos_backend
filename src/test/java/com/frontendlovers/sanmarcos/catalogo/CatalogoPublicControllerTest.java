package com.frontendlovers.sanmarcos.catalogo;

import com.frontendlovers.sanmarcos.entity.enums.TipoCategoria;
import com.frontendlovers.sanmarcos.repository.CategoriaRepository;
import com.frontendlovers.sanmarcos.repository.ProductoRepository;
import com.frontendlovers.sanmarcos.repository.ServicioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

//Verifica que los endpoints públicos del catálogo respondan sin autenticación, que los filtros funcionesn y que los IDs inexistentes devuelvan 404
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CatalogoPublicControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired CategoriaRepository categoriaRepository;
    @Autowired ProductoRepository productoRepository;
    @Autowired ServicioRepository servicioRepository;

    @Test
    void categoriesEsPublicoYDevuelveTodas() throws Exception {
        mockMvc.perform(get("/api/v1/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize((int) categoriaRepository.count())));
    }

    @Test
    void categoriesFiltraPorTipoProducto() throws Exception {
        mockMvc.perform(get("/api/v1/categories?type=PRODUCTO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(4)))
                .andExpect(jsonPath("$[0].tipo").value("PRODUCTO"));
    }

    @Test
    void productsEsPublico() throws Exception {
        mockMvc.perform(get("/api/v1/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize((int) productoRepository.count())));
    }

    @Test
    void productsFiltraPorOferta() throws Exception {
        mockMvc.perform(get("/api/v1/products?onSale=true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].descuento").value(10))
                .andExpect(jsonPath("$[0].precioFinal").exists());
    }

    @Test
    void productsFiltraPorBusqueda() throws Exception {
        mockMvc.perform(get("/api/v1/products?search=croqueta"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void productDetailEsPublico() throws Exception {
        Long id = productoRepository.findByCodigo("ALI-001").orElseThrow().getId();
        mockMvc.perform(get("/api/v1/products/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigo").value("ALI-001"))
                .andExpect(jsonPath("$.categoriaNombre").value("Alimentos"));
    }

    @Test
    void productInexistenteRetorna404() throws Exception {
        mockMvc.perform(get("/api/v1/products/999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void servicesEsPublico() throws Exception {
        mockMvc.perform(get("/api/v1/services"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize((int) servicioRepository.count())));
    }

    @Test
    void servicesFiltraPorBusqueda() throws Exception {
        mockMvc.perform(get("/api/v1/services?search=vacuna"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void serviceDetailEsPublico() throws Exception {
        Long id = servicioRepository.findByCodigo("VAC-001").orElseThrow().getId();
        mockMvc.perform(get("/api/v1/services/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigo").value("VAC-001"))
                .andExpect(jsonPath("$.categoriaNombre").value("Vacunación"));
    }

    @Test
    void serviceInexistenteRetorna404() throws Exception {
        mockMvc.perform(get("/api/v1/services/999999"))
                .andExpect(status().isNotFound());
    }
}