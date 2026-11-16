package com.frontendlovers.sanmarcos.catalogo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.frontendlovers.sanmarcos.entity.Rol;
import com.frontendlovers.sanmarcos.entity.Usuario;
import com.frontendlovers.sanmarcos.entity.enums.TipoCategoria;
import com.frontendlovers.sanmarcos.repository.CategoriaRepository;
import com.frontendlovers.sanmarcos.repository.RolRepository;
import com.frontendlovers.sanmarcos.repository.UsuarioRepository;
import com.frontendlovers.sanmarcos.security.JwtService;
import com.frontendlovers.sanmarcos.security.UsuarioUserDetailsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

//Verifica que los endpoints ded admin requieran token. rechacen usuarios CLIENTE y permitan CRUD al ADMin
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AdminCatalogoPermisosTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired UsuarioRepository usuarioRepository;
    @Autowired RolRepository rolRepository;
    @Autowired CategoriaRepository categoriaRepository;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired JwtService jwtService;
    @Autowired UsuarioUserDetailsService usuarioUserDetailsService;

    private String tokenCliente;
    private String tokenAdmin;
    private Long categoriaProductoId;
    private Long categoriaServicioId;

    @BeforeEach
    void setUp() {
        Rol rolCliente = rolRepository.findByNombre("CLIENTE").orElseThrow();
        Rol rolAdmin = rolRepository.findByNombre("ADMIN").orElseThrow();

        Usuario cliente = Usuario.builder()
                .run("11.111.111-1")
                .email("cliente.catalogo@test.com")
                .password(passwordEncoder.encode("password123"))
                .nombre("Cliente")
                .apellido("Test")
                .rol(rolCliente)
                .build();
        usuarioRepository.save(cliente);

        Usuario admin = Usuario.builder()
                .run("22.222.222-2")
                .email("admin.catalogo@test.com")
                .password(passwordEncoder.encode("password123"))
                .nombre("Admin")
                .apellido("Test")
                .rol(rolAdmin)
                .build();
        usuarioRepository.save(admin);

        tokenCliente = jwtService.generateToken(
                usuarioUserDetailsService.loadUserByUsername(cliente.getEmail()));
        tokenAdmin = jwtService.generateToken(
                usuarioUserDetailsService.loadUserByUsername(admin.getEmail()));

        categoriaProductoId = categoriaRepository
                .findByNombreAndTipo("Alimentos", TipoCategoria.PRODUCTO)
                .orElseThrow().getId();
        categoriaServicioId = categoriaRepository
                .findByNombreAndTipo("Vacunación", TipoCategoria.SERVICIO)
                .orElseThrow().getId();
    }

    @Test
    void adminProductsSinTokenRetorna401() throws Exception {
        mockMvc.perform(get("/api/v1/admin/products"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void adminProductsConClienteRetorna403() throws Exception {
        mockMvc.perform(get("/api/v1/admin/products")
                        .header("Authorization", "Bearer " + tokenCliente))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.detail").value("No tienes permisos para acceder a este recurso"));
    }

    @Test
    void adminProductsConAdminPuedeCrear() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "nombre", "Producto Admin Test",
                "descripcion", "Descripción",
                "presentacion", "Caja",
                "precio", 15000,
                "descuento", 5,
                "stock", 20,
                "stockCritico", 3,
                "categoriaId", categoriaProductoId
        ));

        mockMvc.perform(post("/api/v1/admin/products")
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigo").value(startsWith("PROD-")))
                .andExpect(jsonPath("$.activo").value(true));
    }

    @Test
    void adminPuedeActualizarToggleYEliminarProducto() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "nombre", "Producto CRUD",
                "descripcion", "Desc",
                "presentacion", "Caja",
                "precio", 10000,
                "descuento", 0,
                "stock", 10,
                "stockCritico", 2,
                "categoriaId", categoriaProductoId
        ));

        String response = mockMvc.perform(post("/api/v1/admin/products")
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Long id = objectMapper.readTree(response).get("id").asLong();

        String updateBody = objectMapper.writeValueAsString(Map.of(
                "nombre", "Producto Actualizado",
                "descripcion", "Nueva desc",
                "presentacion", "Bolsa",
                "precio", 12000,
                "descuento", 10,
                "stock", 15,
                "stockCritico", 3,
                "categoriaId", categoriaProductoId
        ));

        mockMvc.perform(put("/api/v1/admin/products/{id}", id)
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Producto Actualizado"))
                .andExpect(jsonPath("$.descuento").value(10));

        mockMvc.perform(patch("/api/v1/admin/products/{id}/toggle", id)
                        .header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activo").value(false));

        mockMvc.perform(delete("/api/v1/admin/products/{id}", id)
                        .header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isNoContent());
    }

    @Test
    void adminServicesConAdminPuedeCrear() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "nombre", "Servicio Admin Test",
                "descripcion", "Descripción",
                "especie", "Perro",
                "duracion", "30 min",
                "precio", 25000,
                "nota", "Requiere previa",
                "categoriaId", categoriaServicioId
        ));

        mockMvc.perform(post("/api/v1/admin/services")
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigo").value(startsWith("SERV-")));
    }

    @Test
    void clienteNoPuedeCrearProducto() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "nombre", "Intento Cliente",
                "precio", 10000,
                "descuento", 0,
                "stock", 1,
                "stockCritico", 1,
                "categoriaId", categoriaProductoId
        ));

        mockMvc.perform(post("/api/v1/admin/products")
                        .header("Authorization", "Bearer " + tokenCliente)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());
    }
}