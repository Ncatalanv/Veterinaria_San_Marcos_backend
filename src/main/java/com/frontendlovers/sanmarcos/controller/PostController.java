package com.frontendlovers.sanmarcos.controller;

import com.frontendlovers.sanmarcos.dto.PostRequest;
import com.frontendlovers.sanmarcos.dto.PostResponse;
import com.frontendlovers.sanmarcos.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/posts")
@Tag(name = "Blog/Posts", description = "Publicaciones del blog")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping
    @Operation(summary = "Listar posts", description = "Pǧblico: lista posts publicados. Admin autenticado: lista todos (incluye no publicados).")
    public Page<PostResponse> listar(
            @PageableDefault(size = 10, sort = "fecha", direction = Sort.Direction.DESC) Pageable pageable) {
        if (isAdmin()) {
            return postService.listarTodos(pageable);
        }
        return postService.listarPublicados(pageable);
    }

    @GetMapping("/{slug}")
    @Operation(summary = "Obtener post por slug", description = "Retorna un post publicado por su slug")
    public PostResponse obtenerPorSlug(@PathVariable String slug) {
        return postService.obtenerPorSlug(slug);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Crear post", description = "Crea un nuevo post (solo ADMIN)")
    public ResponseEntity<PostResponse> crear(@Valid @RequestBody PostRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(postService.crear(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Actualizar post", description = "Actualiza un post existente (solo ADMIN)")
    public PostResponse actualizar(@PathVariable Long id, @Valid @RequestBody PostRequest request) {
        return postService.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Despublicar post", description = "Despublica (soft delete) un post (solo ADMIN)")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        postService.despublicar(id);
        return ResponseEntity.noContent().build();
    }

    private boolean isAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return false;
        return auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
    }
}
