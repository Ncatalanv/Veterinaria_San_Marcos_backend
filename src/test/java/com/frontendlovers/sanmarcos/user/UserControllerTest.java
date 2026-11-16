package com.frontendlovers.sanmarcos.user;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.frontendlovers.sanmarcos.entity.Rol;
import com.frontendlovers.sanmarcos.entity.Usuario;
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

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class UserControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired UsuarioRepository usuarioRepository;
    @Autowired RolRepository rolRepository;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired JwtService jwtService;
    @Autowired UsuarioUserDetailsService usuarioUserDetailsService;

    private String tokenAdmin;
    private String tokenCliente;
    private Usuario usuarioCliente;

    @BeforeEach
    void setUp() {
        Rol rolAdmin = rolRepository.findByNombre("ADMIN").orElseThrow();
        Rol rolCliente = rolRepository.findByNombre("CLIENTE").orElseThrow();

        Usuario admin = Usuario.builder()
                .run("1.111.111-4")
                .email("admin.users@correo.com")
                .password(passwordEncoder.encode("password123"))
                .nombre("Admin")
                .apellido("Users")
                .rol(rolAdmin)
                .build();
        usuarioRepository.save(admin);
        tokenAdmin = jwtService.generateToken(usuarioUserDetailsService.loadUserByUsername(admin.getEmail()));

        usuarioCliente = Usuario.builder()
                .run("2.222.222-8")
                .email("cliente.users@correo.com")
                .password(passwordEncoder.encode("password123"))
                .nombre("Cliente")
                .apellido("Users")
                .rol(rolCliente)
                .build();
        usuarioRepository.save(usuarioCliente);
        tokenCliente = jwtService.generateToken(usuarioUserDetailsService.loadUserByUsername(usuarioCliente.getEmail()));
    }

    private String nuevoUsuarioJson(Map<String, Object> overrides) throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("run", "3.333.333-1");
        body.put("email", "nuevo.users@correo.com");
        body.put("password", "password123");
        body.put("nombre", "Nuevo");
        body.put("apellido", "Usuario");
        body.put("rol", "CLIENTE");
        body.putAll(overrides);
        return objectMapper.writeValueAsString(body);
    }

    // --- Autorizaci��n ---
    @Test
    void listarUsuariosSinTokenRetorna401() throws Exception {
        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void listarUsuariosConClienteRetorna403() throws Exception {
        mockMvc.perform(get("/api/v1/users")
                        .header("Authorization", "Bearer " + tokenCliente))
                .andExpect(status().isForbidden());
    }

    @Test
    void listarUsuariosConAdminRetorna200() throws Exception {
        mockMvc.perform(get("/api/v1/users")
                        .header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    // --- GET by id ---
    @Test
    void obtenerUsuarioPorIdComoAdminRetorna200SinPassword() throws Exception {
        mockMvc.perform(get("/api/v1/users/{id}", usuarioCliente.getId())
                        .header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(usuarioCliente.getId()))
                .andExpect(jsonPath("$.email").value("cliente.users@correo.com"))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    @Test
    void obtenerUsuarioInexistenteRetorna404() throws Exception {
        mockMvc.perform(get("/api/v1/users/{id}", 999999)
                        .header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isNotFound());
    }

    // --- POST ---
    @Test
    void crearUsuarioComoAdminRetorna201SinPassword() throws Exception {
        String response = mockMvc.perform(post("/api/v1/users")
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(nuevoUsuarioJson(Map.of())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("nuevo.users@correo.com"))
                .andExpect(jsonPath("$.rol").value("CLIENTE"))
                .andExpect(jsonPath("$.activo").value(true))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist())
                .andReturn().getResponse().getContentAsString();

        JsonNode json = objectMapper.readTree(response);
        assertThat(json.has("password")).isFalse();
        assertThat(json.has("passwordHash")).isFalse();
    }

    @Test
    void crearUsuarioConRolAsignadoPorAdmin() throws Exception {
        mockMvc.perform(post("/api/v1/users")
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(nuevoUsuarioJson(Map.of("rol", "VENDEDOR", "email", "vend@correo.com", "run", "4.444.444-5"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rol").value("VENDEDOR"));
    }

    @Test
    void crearUsuarioConEmailDuplicadoRetorna409() throws Exception {
        mockMvc.perform(post("/api/v1/users")
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(nuevoUsuarioJson(Map.of("email", "cliente.users@correo.com", "run", "5.555.555-9"))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("El email ya estǭ registrado"));
    }

    @Test
    void crearUsuarioConRunDuplicadoRetorna409() throws Exception {
        mockMvc.perform(post("/api/v1/users")
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(nuevoUsuarioJson(Map.of("run", "2.222.222-8", "email", "otro@correo.com"))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("El RUN ya estǭ registrado"));
    }

    @Test
    void crearUsuarioConEmailInvalidoRetorna400() throws Exception {
        mockMvc.perform(post("/api/v1/users")
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(nuevoUsuarioJson(Map.of("email", "no-es-email"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void crearUsuarioConRunInvalidoRetorna400() throws Exception {
        mockMvc.perform(post("/api/v1/users")
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(nuevoUsuarioJson(Map.of("run", "12.345.678-9"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void crearUsuarioConRolInexistenteRetorna404() throws Exception {
        mockMvc.perform(post("/api/v1/users")
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(nuevoUsuarioJson(Map.of("rol", "NOEXISTE", "email", "rolx@correo.com", "run", "6.666.666-2"))))
                .andExpect(status().isNotFound());
    }

    // --- PUT ---
    @Test
    void actualizarUsuarioComoAdminRetorna200() throws Exception {
        mockMvc.perform(put("/api/v1/users/{id}", usuarioCliente.getId())
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "nombre", "Actualizado",
                                "rol", "VENDEDOR",
                                "activo", false))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Actualizado"))
                .andExpect(jsonPath("$.rol").value("VENDEDOR"))
                .andExpect(jsonPath("$.activo").value(false))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void actualizarUsuarioConEmailDuplicadoRetorna409() throws Exception {
        mockMvc.perform(put("/api/v1/users/{id}", usuarioCliente.getId())
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("email", "admin.users@correo.com"))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("El email ya estǭ registrado"));
    }

    @Test
    void actualizarUsuarioConRunDuplicadoRetorna409() throws Exception {
        mockMvc.perform(put("/api/v1/users/{id}", usuarioCliente.getId())
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("run", "1.111.111-4"))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("El RUN ya estǭ registrado"));
    }

    @Test
    void actualizarUsuarioInexistenteRetorna404() throws Exception {
        mockMvc.perform(put("/api/v1/users/{id}", 999999)
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("nombre", "X"))))
                .andExpect(status().isNotFound());
    }

    // --- DELETE (soft delete) ---
    @Test
    void desactivarUsuarioComoAdminRetorna204YQuedaInactivo() throws Exception {
        mockMvc.perform(delete("/api/v1/users/{id}", usuarioCliente.getId())
                        .header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isNoContent());

        Usuario actualizado = usuarioRepository.findById(usuarioCliente.getId()).orElseThrow();
        assertThat(actualizado.getActivo()).isFalse();
    }

    @Test
    void desactivarUsuarioInexistenteRetorna404() throws Exception {
        mockMvc.perform(delete("/api/v1/users/{id}", 999999)
                        .header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isNotFound());
    }

    @Test
    void crearUsuarioConClienteRetorna403() throws Exception {
        mockMvc.perform(post("/api/v1/users")
                        .header("Authorization", "Bearer " + tokenCliente)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(nuevoUsuarioJson(Map.of("email", "x@correo.com", "run", "7.777.777-6"))))
                .andExpect(status().isForbidden());
    }
}
