package com.frontendlovers.sanmarcos.solicitud;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.frontendlovers.sanmarcos.dto.SolicitudCitaRequest;
import com.frontendlovers.sanmarcos.dto.SolicitudComentarioRequest;
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
import java.time.LocalTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class SolicitudServicioUpdateTest {

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
    private String tokenVeterinario;
    private String tokenAdmin;
    private String tokenVendedor;
    private Long solicitudId;
    private Long servicioId;

    @BeforeEach
    void setUp() {
        String suffix = String.valueOf(System.currentTimeMillis());
        Usuario cliente = crearUsuario("33.333.333-3", "CLIENTE", suffix);
        Usuario veterinario = crearUsuario("77.777.777-7", "VETERINARIO", suffix);
        Usuario admin = crearUsuario("22.222.222-2", "ADMIN", suffix);
        Usuario vendedor = crearUsuario("66.666.666-6", "VENDEDOR", suffix);

        tokenCliente = generarToken(cliente.getEmail());
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
    void veterinarioConfirmaSolicitud() throws Exception {
        String body = objectMapper.writeValueAsString(new SolicitudEstadoRequest(EstadoSolicitud.CONFIRMADA));

        mockMvc.perform(patch("/api/v1/service-requests/{id}/status", solicitudId)
                        .header("Authorization", "Bearer " + tokenVeterinario)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("CONFIRMADA"));
    }

    @Test
    void veterinarioCompletaSolicitud() throws Exception {
        solicitudService.cambiarEstado(solicitudId, EstadoSolicitud.CONFIRMADA);
        String body = objectMapper.writeValueAsString(new SolicitudEstadoRequest(EstadoSolicitud.COMPLETADA));

        mockMvc.perform(patch("/api/v1/service-requests/{id}/status", solicitudId)
                        .header("Authorization", "Bearer " + tokenVeterinario)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("COMPLETADA"));
    }

    @Test
    void transicionInvalidaDevuelve409() throws Exception {
        String body = objectMapper.writeValueAsString(new SolicitudEstadoRequest(EstadoSolicitud.COMPLETADA));

        mockMvc.perform(patch("/api/v1/service-requests/{id}/status", solicitudId)
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict());
    }

    @Test
    void clienteCancelaSolicitudPendiente() throws Exception {
        mockMvc.perform(patch("/api/v1/service-requests/{id}/cancel", solicitudId)
                        .header("Authorization", "Bearer " + tokenCliente))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("CANCELADA"));
    }

    @Test
    void clienteNoCancelaSolicitudConfirmada() throws Exception {
        solicitudService.cambiarEstado(solicitudId, EstadoSolicitud.CONFIRMADA);

        mockMvc.perform(patch("/api/v1/service-requests/{id}/cancel", solicitudId)
                        .header("Authorization", "Bearer " + tokenCliente))
                .andExpect(status().isConflict());
    }

    @Test
    void clienteNoCambiaEstado() throws Exception {
        String body = objectMapper.writeValueAsString(new SolicitudEstadoRequest(EstadoSolicitud.CONFIRMADA));

        mockMvc.perform(patch("/api/v1/service-requests/{id}/status", solicitudId)
                        .header("Authorization", "Bearer " + tokenCliente)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());
    }

    @Test
    void vendedorNoActualizaSolicitudes() throws Exception {
        String body = objectMapper.writeValueAsString(new SolicitudEstadoRequest(EstadoSolicitud.CONFIRMADA));

        mockMvc.perform(patch("/api/v1/service-requests/{id}/status", solicitudId)
                        .header("Authorization", "Bearer " + tokenVendedor)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminActualizaFechaYComentario() throws Exception {
        String citaBody = objectMapper.writeValueAsString(
                new SolicitudCitaRequest(LocalDate.of(2026, 12, 25), LocalTime.of(10, 30)));

        mockMvc.perform(patch("/api/v1/service-requests/{id}/date-time", solicitudId)
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(citaBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fechaCita").value("2026-12-25"))
                .andExpect(jsonPath("$.horaCita").value("10:30:00"));

        String comentarioBody = objectMapper.writeValueAsString(
                new SolicitudComentarioRequest("Comentario actualizado"));

        mockMvc.perform(patch("/api/v1/service-requests/{id}/comment", solicitudId)
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(comentarioBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.comentario").value("Comentario actualizado"));
    }
}
