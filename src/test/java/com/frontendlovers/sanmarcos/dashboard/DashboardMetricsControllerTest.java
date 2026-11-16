package com.frontendlovers.sanmarcos.dashboard;

import com.frontendlovers.sanmarcos.entity.Orden;
import com.frontendlovers.sanmarcos.entity.Producto;
import com.frontendlovers.sanmarcos.entity.Rol;
import com.frontendlovers.sanmarcos.entity.Servicio;
import com.frontendlovers.sanmarcos.entity.SolicitudServicio;
import com.frontendlovers.sanmarcos.entity.Usuario;
import com.frontendlovers.sanmarcos.entity.enums.EstadoOrden;
import com.frontendlovers.sanmarcos.entity.enums.EstadoSolicitud;
import com.frontendlovers.sanmarcos.entity.enums.TipoEntrega;
import com.frontendlovers.sanmarcos.repository.OrdenRepository;
import com.frontendlovers.sanmarcos.repository.ProductoRepository;
import com.frontendlovers.sanmarcos.repository.RolRepository;
import com.frontendlovers.sanmarcos.repository.ServicioRepository;
import com.frontendlovers.sanmarcos.repository.SolicitudServicioRepository;
import com.frontendlovers.sanmarcos.repository.UsuarioRepository;
import com.frontendlovers.sanmarcos.security.JwtService;
import com.frontendlovers.sanmarcos.security.UsuarioUserDetailsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class DashboardMetricsControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired UsuarioRepository usuarioRepository;
    @Autowired RolRepository rolRepository;
    @Autowired ProductoRepository productoRepository;
    @Autowired OrdenRepository ordenRepository;
    @Autowired ServicioRepository servicioRepository;
    @Autowired SolicitudServicioRepository solicitudRepository;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired JwtService jwtService;
    @Autowired UsuarioUserDetailsService usuarioUserDetailsService;

    private String tokenAdmin;
    private String tokenCliente;

    @BeforeEach
    void setUp() {
        Rol rolAdmin = rolRepository.findByNombre("ADMIN").orElseThrow();
        Rol rolCliente = rolRepository.findByNombre("CLIENTE").orElseThrow();

        Usuario admin = usuarioRepository.save(Usuario.builder()
                .run("30.000.000-3").email("admin.dash@correo.com")
                .password(passwordEncoder.encode("password123"))
                .nombre("Admin").apellido("Dash").rol(rolAdmin).build());
        tokenAdmin = jwtService.generateToken(usuarioUserDetailsService.loadUserByUsername(admin.getEmail()));

        Usuario cliente = usuarioRepository.save(Usuario.builder()
                .run("40.000.000-4").email("cliente.dash@correo.com")
                .password(passwordEncoder.encode("password123"))
                .nombre("Cliente").apellido("Dash").rol(rolCliente).build());
        tokenCliente = jwtService.generateToken(usuarioUserDetailsService.loadUserByUsername(cliente.getEmail()));

        productoRepository.save(Producto.builder()
                .codigo("DASH-CRIT").nombre("Prod Critico").precio(new BigDecimal("1000"))
                .descuento(0).stock(2).stockCritico(5).activo(true).build());
        productoRepository.save(Producto.builder()
                .codigo("DASH-OK").nombre("Prod Ok").precio(new BigDecimal("1000"))
                .descuento(0).stock(10).stockCritico(5).activo(true).build());

        ordenRepository.save(Orden.builder()
                .numeroOrden("ORD-DASH-1").usuario(cliente)
                .estado(EstadoOrden.PAGADA).total(new BigDecimal("5000"))
                .direccionEnvio("Calle 1").tipoEntrega(TipoEntrega.RETIRO).build());

        Servicio servicio = servicioRepository.findAll().stream().findFirst().orElse(null);
        if (servicio != null) {
            solicitudRepository.save(SolicitudServicio.builder()
                    .usuario(cliente).servicio(servicio)
                    .mascotaNombre("Firulais").especie("Perro")
                    .fechaSolicitada(LocalDate.of(2026, 6, 1))
                    .estado(EstadoSolicitud.PENDIENTE).build());
        }
    }

    @Test
    void metricsSinTokenRetorna401() throws Exception {
        mockMvc.perform(get("/api/v1/dashboard/metrics"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void metricsConClienteRetorna403() throws Exception {
        mockMvc.perform(get("/api/v1/dashboard/metrics")
                        .header("Authorization", "Bearer " + tokenCliente))
                .andExpect(status().isForbidden());
    }

    @Test
    void metricsConAdminRetorna200YEstructura() throws Exception {
        mockMvc.perform(get("/api/v1/dashboard/metrics")
                        .header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalOrdenes").isNumber())
                .andExpect(jsonPath("$.totalSolicitudes").isNumber())
                .andExpect(jsonPath("$.totalUsuarios").isNumber())
                .andExpect(jsonPath("$.totalProductos").isNumber())
                .andExpect(jsonPath("$.productosStockCritico").isArray())
                .andExpect(jsonPath("$.ultimasOrdenes").isArray())
                .andExpect(jsonPath("$.ultimasSolicitudes").isArray())
                .andExpect(jsonPath("$.productosStockCritico[?(@.codigo=='DASH-CRIT')]").isNotEmpty())
                .andExpect(jsonPath("$.productosStockCritico[?(@.codigo=='DASH-OK')]").isEmpty())
                .andExpect(jsonPath("$.ultimasOrdenes[0].numeroOrden").value("ORD-DASH-1"))
                .andExpect(jsonPath("$.ultimasOrdenes[0].clienteNombre").value("Cliente Dash"))
                .andExpect(jsonPath("$.ultimasOrdenes[0].fechaOrden").exists())
                .andExpect(jsonPath("$.ultimasOrdenes[0].estado").value("PAGADA"));
    }

    @Test
    void metricsIncluyeSolicitudSiHayServicio() throws Exception {
        if (servicioRepository.findAll().isEmpty()) return;
        mockMvc.perform(get("/api/v1/dashboard/metrics")
                        .header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ultimasSolicitudes[0].clienteNombre").value("Cliente Dash"))
                .andExpect(jsonPath("$.ultimasSolicitudes[0].estado").value("PENDIENTE"))
                .andExpect(jsonPath("$.ultimasSolicitudes[0].servicioNombre").isNotEmpty());
    }
}
