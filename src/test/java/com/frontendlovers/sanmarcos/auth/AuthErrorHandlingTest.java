package com.frontendlovers.sanmarcos.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.frontendlovers.sanmarcos.entity.Mascota;
import com.frontendlovers.sanmarcos.entity.Usuario;
import com.frontendlovers.sanmarcos.repository.MascotaRepository;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional // cada test hará rollback --> así no queda nada en la bd
class AuthErrorHandlingTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired UsuarioRepository usuarioRepository;
    @Autowired RolRepository rolRepository;
    @Autowired MascotaRepository mascotaRepository;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired JwtService jwtService;
    @Autowired UsuarioUserDetailsService usuarioUserDetailsService;

    private String tokenCliente;

    @BeforeEach
    void setUp() {
        Usuario usuario = Usuario.builder()
                .run("11.111.111-1")
                .email("cliente.test@correo.com")
                .password(passwordEncoder.encode("password123"))
                .nombre("Cliente")
                .apellido("Test")
                .rol(rolRepository.findByNombre("CLIENTE").orElseThrow())
                .build();
        usuarioRepository.save(usuario);
        tokenCliente = jwtService.generateToken(
                usuarioUserDetailsService.loadUserByUsername(usuario.getEmail()));
    }

    private String registroJson(Map<String, Object> overrides) throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("run", "6.000.000-K");
        body.put("email", "nuevo@correo.com");
        body.put("password", "password123");
        body.put("nombre", "Nuevo");
        body.put("apellido", "Usuario");
        body.putAll(overrides);
        return objectMapper.writeValueAsString(body);
    }

    // ─── 400 

    @Test
    void registroConEmailInvalidoRetorna400() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registroJson(Map.of("email", "no-es-un-email"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    void registroConPasswordCortaRetorna400() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registroJson(Map.of("password", "123"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registroConRunInvalidoRetorna400() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registroJson(Map.of("run", "12.345.678-9"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("run"));
    }

    @Test
    void jsonMalformadoRetorna400() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{json-roto"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Cuerpo de la solicitud inválido o malformado"));
    }

    // ─── 401 

    @Test
    void loginConPasswordIncorrectaRetorna401() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"cliente.test@correo.com","password":"otra-clave"}
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("Credenciales inválidas"));
    }

    @Test
    void loginConEmailInexistenteRetorna401() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"fantasma@correo.com","password":"password123"}
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("Credenciales inválidas"));
    }

    @Test
    void meSinTokenRetorna401() throws Exception {
        mockMvc.perform(get("/api/v1/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("Token faltante o inválido"));
    }

    @Test
    void meConTokenBasuraRetorna401() throws Exception {
        mockMvc.perform(get("/api/v1/auth/me")
                        .header("Authorization", "Bearer abc.def.ghi"))
                .andExpect(status().isUnauthorized());
    }

    // ─── 403 

    @Test
    void endpointAdminConClienteRetorna403() throws Exception {
        mockMvc.perform(get("/api/v1/test-solo-admin")
                        .header("Authorization", "Bearer " + tokenCliente))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.detail").value("No tienes permisos para acceder a este recurso"));
    }

    // ─── 404

    @Test
    void urlInexistenteRetorna404() throws Exception {
        mockMvc.perform(get("/api/v1/no-existe-esto")
                        .header("Authorization", "Bearer " + tokenCliente))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("El recurso solicitado no existe"));
    }

    // ─── 409 

    @Test
    void registroConEmailDuplicadoRetorna409() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registroJson(Map.of("email", "cliente.test@correo.com"))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("El email ya está registrado"));
    }

    // ─── registro con mascota opcional

    @Test
    void registroSinMascotaRetorna201ConToken() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registroJson(Map.of())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isString())
                .andExpect(jsonPath("$.usuario.email").value("nuevo@correo.com"))
                .andExpect(jsonPath("$.usuario.rol").value("CLIENTE"));

        Usuario usuario = usuarioRepository.findByEmail("nuevo@correo.com").orElseThrow();
        org.assertj.core.api.Assertions.assertThat(mascotaRepository.findByUsuarioId(usuario.getId())).isEmpty();
    }

    @Test
    void registroConMascotaRetorna201YGuardaMascota() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registroJson(Map.of("mascota", Map.of(
                                "nombre", "Firulais",
                                "especie", "Perro",
                                "raza", "Mestizo")))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isString());

        Usuario usuario = usuarioRepository.findByEmail("nuevo@correo.com").orElseThrow();
        java.util.List<Mascota> mascotas = mascotaRepository.findByUsuarioId(usuario.getId());
        org.assertj.core.api.Assertions.assertThat(mascotas).hasSize(1);
        org.assertj.core.api.Assertions.assertThat(mascotas.get(0).getNombre()).isEqualTo("Firulais");
        org.assertj.core.api.Assertions.assertThat(mascotas.get(0).getEspecie()).isEqualTo("Perro");
    }

    @Test
    void registroConMascotaSinNombreRetorna400() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registroJson(Map.of("mascota", Map.of(
                                "nombre", "",
                                "especie", "Perro")))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isArray());
    }

    // flujos correctos y acceso público

    @Test
    void loginCorrectoRetorna200ConToken() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"cliente.test@correo.com","password":"password123"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isString())
                .andExpect(jsonPath("$.usuario.email").value("cliente.test@correo.com"));
    }

    @Test
    void swaggerEsPublico() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk());
    }
}