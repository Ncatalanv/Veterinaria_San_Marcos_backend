package com.frontendlovers.sanmarcos.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.frontendlovers.sanmarcos.entity.Post;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record PostResponse(
        Long id,
        String slug,
        String titulo,
        String categoria,
        String extracto,
        String imagen,
        String alt,
        LocalDate fecha,
        JsonNode contenido,
        Boolean publicado,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static PostResponse from(Post post) {
        return new PostResponse(
                post.getId(),
                post.getSlug(),
                post.getTitulo(),
                post.getCategoria(),
                post.getExtracto(),
                post.getImagen(),
                post.getAlt(),
                post.getFecha(),
                readContenido(post.getContenido()),
                post.getPublicado(),
                post.getCreatedAt(),
                post.getUpdatedAt()
        );
    }

    private static JsonNode readContenido(String contenido) {
        if (contenido == null) return null;
        try {
            return MAPPER.readTree(contenido);
        } catch (Exception e) {
            return MAPPER.getNodeFactory().textNode(contenido);
        }
    }
}
