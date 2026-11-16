package com.frontendlovers.sanmarcos.post;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.frontendlovers.sanmarcos.entity.Post;
import com.frontendlovers.sanmarcos.entity.Rol;
import com.frontendlovers.sanmarcos.entity.Usuario;
import com.frontendlovers.sanmarcos.repository.PostRepository;
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

import java.time.LocalDate;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PostControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired PostRepository postRepository;
    @Autowired UsuarioRepository usuarioRepository;
    @Autowired RolRepository rolRepository;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired JwtService jwtService;
    @Autowired UsuarioUserDetailsService usuarioUserDetailsService;

    private String tokenAdmin;
    private String tokenCliente;
    private Post publicado;
    private Post borrador;

    @BeforeEach
    void setUp() {
        Rol rolAdmin = rolRepository.findByNombre("ADMIN").orElseThrow();
        Rol rolCliente = rolRepository.findByNombre("CLIENTE").orElseThrow();

        Usuario admin = Usuario.builder()
                .run("10.000.000-1")
                .email("admin.posts@correo.com")
                .password(passwordEncoder.encode("password123"))
                .nombre("Admin").apellido("Posts").rol(rolAdmin).build();
        usuarioRepository.save(admin);
        tokenAdmin = jwtService.generateToken(usuarioUserDetailsService.loadUserByUsername(admin.getEmail()));

        Usuario cliente = Usuario.builder()
                .run("20.000.000-2")
                .email("cliente.posts@correo.com")
                .password(passwordEncoder.encode("password123"))
                .nombre("Cliente").apellido("Posts").rol(rolCliente).build();
        usuarioRepository.save(cliente);
        tokenCliente = jwtService.generateToken(usuarioUserDetailsService.loadUserByUsername(cliente.getEmail()));

        publicado = postRepository.save(Post.builder()
                .slug("post-publicado-test")
                .titulo("Post Publicado")
                .categoria("Salud")
                .extracto("Extracto publicado")
                .fecha(LocalDate.of(2026, 1, 10))
                .contenido("[{\"type\":\"paragraph\",\"text\":\"Hola\"}]")
                .publicado(true)
                .build());

        borrador = postRepository.save(Post.builder()
                .slug("post-borrador-test")
                .titulo("Post Borrador")
                .categoria("Salud")
                .extracto("Extracto borrador")
                .fecha(LocalDate.of(2026, 1, 11))
                .contenido("[{\"type\":\"paragraph\",\"text\":\"Borrador\"}]")
                .publicado(false)
                .build());
    }

    private String requestJson(Map<String, Object> overrides) throws Exception {
        Map<String, Object> body = new java.util.HashMap<>();
        body.put("slug", "nuevo-post-test");
        body.put("titulo", "Nuevo Post");
        body.put("categoria", "Consejos");
        body.put("extracto", "Extracto");
        body.put("imagen", "/img.jpg");
        body.put("alt", "alt");
        body.put("fecha", "2026-06-01");
        body.put("contenido", objectMapper.readTree("[{\"type\":\"paragraph\",\"text\":\"Contenido\"}]"));
        body.put("publicado", true);
        body.putAll(overrides);
        return objectMapper.writeValueAsString(body);
    }

    // --- Pǧblico ---
    @Test
    void listarPublicoRetornaSoloPublicados() throws Exception {
        mockMvc.perform(get("/api/v1/posts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[?(@.slug=='post-borrador-test')]").isEmpty())
                .andExpect(jsonPath("$.content[?(@.slug=='post-publicado-test')]").isNotEmpty());
    }

    @Test
    void listarComoAdminIncluyeNoPublicados() throws Exception {
        mockMvc.perform(get("/api/v1/posts")
                        .header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[?(@.slug=='post-borrador-test')]").isNotEmpty());
    }

    @Test
    void obtenerPorSlugPublicadoRetorna200() throws Exception {
        mockMvc.perform(get("/api/v1/posts/{slug}", "post-publicado-test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.slug").value("post-publicado-test"))
                .andExpect(jsonPath("$.titulo").value("Post Publicado"));
    }

    @Test
    void obtenerPorSlugNoPublicadoRetorna404() throws Exception {
        mockMvc.perform(get("/api/v1/posts/{slug}", "post-borrador-test"))
                .andExpect(status().isNotFound());
    }

    @Test
    void obtenerPorSlugInexistenteRetorna404() throws Exception {
        mockMvc.perform(get("/api/v1/posts/{slug}", "no-existe"))
                .andExpect(status().isNotFound());
    }

    // --- AuthZ ---
    @Test
    void crearSinTokenRetorna401() throws Exception {
        mockMvc.perform(post("/api/v1/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson(Map.of())))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void crearConClienteRetorna403() throws Exception {
        mockMvc.perform(post("/api/v1/posts")
                        .header("Authorization", "Bearer " + tokenCliente)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson(Map.of())))
                .andExpect(status().isForbidden());
    }

    // --- CRUD admin ---
    @Test
    void crearComoAdminRetorna201() throws Exception {
        mockMvc.perform(post("/api/v1/posts")
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson(Map.of())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.slug").value("nuevo-post-test"))
                .andExpect(jsonPath("$.publicado").value(true))
                .andExpect(jsonPath("$.contenido").isArray());
    }

    @Test
    void crearConSlugDuplicadoRetorna409() throws Exception {
        mockMvc.perform(post("/api/v1/posts")
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson(Map.of("slug", "post-publicado-test"))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("El slug ya estǭ registrado"));
    }

    @Test
    void crearConDatosInvalidosRetorna400() throws Exception {
        mockMvc.perform(post("/api/v1/posts")
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson(Map.of("slug", "", "titulo", ""))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void actualizarComoAdminRetorna200() throws Exception {
        mockMvc.perform(put("/api/v1/posts/{id}", publicado.getId())
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson(Map.of("slug", "post-publicado-test", "titulo", "Titulo Actualizado"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("Titulo Actualizado"));
    }

    @Test
    void actualizarConSlugDuplicadoRetorna409() throws Exception {
        mockMvc.perform(put("/api/v1/posts/{id}", publicado.getId())
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson(Map.of("slug", "post-borrador-test"))))
                .andExpect(status().isConflict());
    }

    @Test
    void actualizarInexistenteRetorna404() throws Exception {
        mockMvc.perform(put("/api/v1/posts/{id}", 999999)
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson(Map.of())))
                .andExpect(status().isNotFound());
    }

    @Test
    void eliminarComoAdminDespublicaYRetorna204() throws Exception {
        mockMvc.perform(delete("/api/v1/posts/{id}", publicado.getId())
                        .header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isNoContent());

        Post actualizado = postRepository.findById(publicado.getId()).orElseThrow();
        assertThat(actualizado.getPublicado()).isFalse();

        mockMvc.perform(get("/api/v1/posts/{slug}", "post-publicado-test"))
                .andExpect(status().isNotFound());
    }

    @Test
    void eliminarInexistenteRetorna404() throws Exception {
        mockMvc.perform(delete("/api/v1/posts/{id}", 999999)
                        .header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isNotFound());
    }
}
