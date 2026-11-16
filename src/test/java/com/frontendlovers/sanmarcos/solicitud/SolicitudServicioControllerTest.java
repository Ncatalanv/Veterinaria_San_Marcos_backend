package com.frontendlovers.sanmarcos.solicitud;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.frontendlovers.sanmarcos.dto.SolicitudEstadoRequest;
import com.frontendlovers.sanmarcos.dto.SolicitudServicioRequest;
import com.frontendlovers.sanmarcos.dto.SolicitudServicioResponse;
import com.frontendlovers.sanmarcos.entity.Usuario;
import com.frontendlovers.sanmarcos.entity.enums.EstadoSolicitud;
import com.frontendlovers.sanmarcos.repository.RolRepository;
import com.frontendlovers.sanmarcos.repository.ServicioRepository;
import com.frontendlovers.sanmarcos.repository.UsuarioRepository;
import com.frontendlovers.sanmarcos.security.JwtService;
import com.frontendlovers.sanmarcos.security.UsuarioUserDetailsService;
import com.frontendlovers.sanmarcos.service.SolicitudServicioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class SolicitudServicioControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired UsuarioRepository usuarioRepository;
    @Autowired RolRepository rolRepository;
    @Autowired ServicioRepository servicioRepository;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired JwtService jwtService;
    @Autowired UsuarioUserDetailsService usuarioUserDetailsService;
    @Autowired SolicitudServicioService solicitudService;

    private String tokenCliente;
    private String tokenCliente2;
    private String tokenVeterinario;
    private String tokenAdmin;
    private String tokenVendedor;
    private Usuario cliente2;
    private Long solicitudId;
    private Long servicioId;

    @BeforeEach
    void setUp() {
        String suffix = String.valueOf(System.currentTimeMillis());
        Usuario cliente = crearUsuario("33.333.333-3", "CLIENTE", suffix);
        cliente2 = crearUsuario("55.555.555-5", "CLIENTE", suffix + "b");
        Usuario veterinario = crearUsuario("77.777.777-7", "VETERINARIO", suffix);
        Usuario admin = crearUsuario("22.222.222-2", "ADMIN", suffix);
        Usuario vendedor = crearUsuario("66.666.666-6", "VENDEDOR", suffix);

        tokenCliente = generarToken(cliente.getEmail());
        tokenCliente2 = generarToken(cliente2.getEmail());
        tokenVeterinario = generarToken(veterinario.getEmail());
        tokenAdmin = generarToken(admin.getEmail());
        tokenVendedor = generarToken(vendedor.getEmail());

        servicioId = servicioRepository.findByCodigo("VAC-001").orElseThrow().getId();

        SolicitudServicioResponse solicitud = solicitudService.crear(cliente.getId(), solicitudRequest());
        solicitudId = solicitud.id();
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

    private SolicitudServicioRequest solicitudRequest() {
        return new SolicitudServicioRequest(
                servicioId,
                null,
                "Firulais",
                "Perro",
                LocalDate.now().plusDays(3),
                "Necesita vacuna anual"
        );
    }

    @Test
    void clientePuedeCrearSolicitud() throws Exception {
        String body = objectMapper.writeValueAsString(solicitudRequest());

        mockMvc.perform(post("/api/v1/service-requests")
                        .header("Authorization", "Bearer " + tokenCliente)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("PENDIENTE"))
                .andExpect(jsonPath("$.mascotaNombre").value("Firulais"));
    }

    @Test
    void clientePuedeVerMisSolicitudes() throws Exception {
        mockMvc.perform(get("/api/v1/service-requests/me")
                        .header("Authorization", "Bearer " + tokenCliente))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void clienteNoPuedeListarTodas() throws Exception {
        mockMvc.perform(get("/api/v1/service-requests")
                        .header("Authorization", "Bearer " + tokenCliente))
                .andExpect(status().isForbidden());
    }

    @Test
    void veterinarioPuedeListarTodas() throws Exception {
        mockMvc.perform(get("/api/v1/service-requests")
                        .header("Authorization", "Bearer " + tokenVeterinario))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void adminPuedeListarTodas() throws Exception {
        mockMvc.perform(get("/api/v1/service-requests")
                        .header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void clienteNoPuedeVerSolicitudAjena() throws Exception {
        mockMvc.perform(get("/api/v1/service-requests/{id}", solicitudId)
                        .header("Authorization", "Bearer " + tokenCliente2))
                .andExpect(status().isForbidden());
    }

    @Test
    void vendedorNoPuedeAccederASolicitudes() throws Exception {
        mockMvc.perform(get("/api/v1/service-requests")
                        .header("Authorization", "Bearer " + tokenVendedor))
                .andExpect(status().isForbidden());
    }

    @Test
    void solicitudConServicioInexistenteRetorna404() throws Exception {
        SolicitudServicioRequest request = new SolicitudServicioRequest(
                999999L, null, "Firulais", "Perro", LocalDate.now().plusDays(3), "");
        String body = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/api/v1/service-requests")
                        .header("Authorization", "Bearer " + tokenCliente)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());
    }

    @Test
    void clienteSoloVeSusSolicitudes() throws Exception {
        solicitudService.crear(cliente2.getId(), solicitudRequest());

        mockMvc.perform(get("/api/v1/service-requests/me")
                        .header("Authorization", "Bearer " + tokenCliente))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(solicitudId.intValue()));
    }

    @Test
    void adminPuedeActualizarEstado() throws Exception {
        String body = objectMapper.writeValueAsString(new SolicitudEstadoRequest(EstadoSolicitud.CONFIRMADA));

        mockMvc.perform(patch("/api/v1/service-requests/{id}/status", solicitudId)
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("CONFIRMADA"));
    }
}
