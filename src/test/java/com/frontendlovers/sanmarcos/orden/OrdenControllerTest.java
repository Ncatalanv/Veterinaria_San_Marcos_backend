package com.frontendlovers.sanmarcos.orden;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.frontendlovers.sanmarcos.dto.OrdenEstadoRequest;
import com.frontendlovers.sanmarcos.dto.OrdenItemRequest;
import com.frontendlovers.sanmarcos.dto.OrdenRequest;
import com.frontendlovers.sanmarcos.dto.OrdenResponse;
import com.frontendlovers.sanmarcos.entity.Usuario;
import com.frontendlovers.sanmarcos.entity.enums.EstadoOrden;
import com.frontendlovers.sanmarcos.entity.enums.TipoEntrega;
import com.frontendlovers.sanmarcos.repository.ProductoRepository;
import com.frontendlovers.sanmarcos.repository.RolRepository;
import com.frontendlovers.sanmarcos.repository.UsuarioRepository;
import com.frontendlovers.sanmarcos.security.JwtService;
import com.frontendlovers.sanmarcos.security.UsuarioUserDetailsService;
import com.frontendlovers.sanmarcos.service.OrdenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class OrdenControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired UsuarioRepository usuarioRepository;
    @Autowired RolRepository rolRepository;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired JwtService jwtService;
    @Autowired UsuarioUserDetailsService usuarioUserDetailsService;
    @Autowired OrdenService ordenService;
    @Autowired ProductoRepository productoRepository;

    private String tokenCliente;
    private String tokenCliente2;
    private String tokenVendedor;
    private String tokenAdmin;
    private String tokenVeterinario;
    private Usuario cliente;
    private Usuario cliente2;
    private Long ordenId;

    @BeforeEach
    void setUp() {
        String suffix = String.valueOf(System.currentTimeMillis());
        cliente = crearUsuario("33.333.333-3", "CLIENTE", suffix);
        cliente2 = crearUsuario("55.555.555-5", "CLIENTE", suffix + "b");
        Usuario vendedor = crearUsuario("66.666.666-6", "VENDEDOR", suffix);
        Usuario admin = crearUsuario("22.222.222-2", "ADMIN", suffix);
        Usuario veterinario = crearUsuario("77.777.777-7", "VETERINARIO", suffix);

        tokenCliente = generarToken(cliente.getEmail());
        tokenCliente2 = generarToken(cliente2.getEmail());
        tokenVendedor = generarToken(vendedor.getEmail());
        tokenAdmin = generarToken(admin.getEmail());
        tokenVeterinario = generarToken(veterinario.getEmail());

        OrdenResponse orden = ordenService.crearOrden(cliente.getId(), ordenRequest(2));
        ordenId = orden.id();
    }

    private Usuario crearUsuario(String run, String rol, String suffix) {
        return usuarioRepository.save(Usuario.builder()
                .run(run)
                .email(rol.toLowerCase() + "." + suffix + "@test.com")
                .password(passwordEncoder.encode("password123"))
                .nombre(rol)
                .apellido("Test")
                .rol(rolRepository.findByNombre(rol).orElseThrow())
                .build());
    }

    private String generarToken(String email) {
        return jwtService.generateToken(usuarioUserDetailsService.loadUserByUsername(email));
    }

    private OrdenRequest ordenRequest(int cantidad) {
        Long productoId = productoRepository.findByCodigo("ALI-001").orElseThrow().getId();
        return new OrdenRequest(
                "Av. Siempre Viva 123",
                TipoEntrega.ENVIO,
                "Dejar en conserjería",
                "Contacto",
                "Orden",
                "test@correo.com",
                "+56912345678",
                List.of(new OrdenItemRequest(productoId, cantidad))
        );
    }

    @Test
    void clientePuedeCrearOrden() throws Exception {
        String body = objectMapper.writeValueAsString(ordenRequest(1));

        mockMvc.perform(post("/api/v1/orders")
                        .header("Authorization", "Bearer " + tokenCliente)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.numeroOrden").value(startsWith("ORD-")))
                .andExpect(jsonPath("$.estado").value("PAGADA"));
    }

    @Test
    void clientePuedeVerMisOrdenes() throws Exception {
        mockMvc.perform(get("/api/v1/orders/me")
                        .header("Authorization", "Bearer " + tokenCliente))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void clienteNoPuedeVerTodasLasOrdenes() throws Exception {
        mockMvc.perform(get("/api/v1/orders")
                        .header("Authorization", "Bearer " + tokenCliente))
                .andExpect(status().isForbidden());
    }

    @Test
    void vendedorPuedeListarTodas() throws Exception {
        mockMvc.perform(get("/api/v1/orders")
                        .header("Authorization", "Bearer " + tokenVendedor))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void adminPuedeCambiarEstado() throws Exception {
        String body = objectMapper.writeValueAsString(new OrdenEstadoRequest(EstadoOrden.EN_PREPARACION));

        mockMvc.perform(patch("/api/v1/orders/{id}/status", ordenId)
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("EN_PREPARACION"));
    }

    @Test
    void vendedorNoPuedeCambiarEstado() throws Exception {
        String body = objectMapper.writeValueAsString(new OrdenEstadoRequest(EstadoOrden.EN_PREPARACION));

        mockMvc.perform(patch("/api/v1/orders/{id}/status", ordenId)
                        .header("Authorization", "Bearer " + tokenVendedor)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());
    }

    @Test
    void clienteNoPuedeCambiarEstado() throws Exception {
        String body = objectMapper.writeValueAsString(new OrdenEstadoRequest(EstadoOrden.EN_PREPARACION));

        mockMvc.perform(patch("/api/v1/orders/{id}/status", ordenId)
                        .header("Authorization", "Bearer " + tokenCliente)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());
    }

    @Test
    void clientePuedeCancelarSuOrden() throws Exception {
        mockMvc.perform(patch("/api/v1/orders/{id}/cancel", ordenId)
                        .header("Authorization", "Bearer " + tokenCliente))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("CANCELADA"));
    }

    @Test
    void clienteNoPuedeCancelarOrdenAjena() throws Exception {
        OrdenResponse ordenAjena = ordenService.crearOrden(cliente2.getId(), ordenRequest(1));

        mockMvc.perform(patch("/api/v1/orders/{id}/cancel", ordenAjena.id())
                        .header("Authorization", "Bearer " + tokenCliente))
                .andExpect(status().isConflict());
    }

    @Test
    void veterinarioNoPuedeAccederAOrdenes() throws Exception {
        mockMvc.perform(get("/api/v1/orders")
                        .header("Authorization", "Bearer " + tokenVeterinario))
                .andExpect(status().isForbidden());
    }

    @Test
    void clienteNoPuedeVerDetalleDeOrdenAjena() throws Exception {
        OrdenResponse ordenAjena = ordenService.crearOrden(cliente2.getId(), ordenRequest(1));

        mockMvc.perform(get("/api/v1/orders/{id}", ordenAjena.id())
                        .header("Authorization", "Bearer " + tokenCliente))
                .andExpect(status().isForbidden());
    }

    @Test
    void ordenValidaDescuentaStockViaEndpoint() throws Exception {
        int cantidad = 3;
        int stockAntes = productoRepository.findByCodigo("ALI-001").orElseThrow().getStock();
        String body = objectMapper.writeValueAsString(ordenRequest(cantidad));

        mockMvc.perform(post("/api/v1/orders")
                        .header("Authorization", "Bearer " + tokenCliente)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("PAGADA"));

        int stockDespues = productoRepository.findByCodigo("ALI-001").orElseThrow().getStock();
        assertThat(stockDespues).isEqualTo(stockAntes - cantidad);
    }
}
