package com.frontendlovers.sanmarcos.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "posts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "slug", nullable = false, unique = true, length = 150)
    private String slug;

    @Column(name = "titulo", nullable = false, length = 200)
    private String titulo;

    @Column(name = "categoria", length = 80)
    private String categoria;

    @Column(name = "extracto", length = 500)
    private String extracto;

    @Column(name = "imagen", length = 255)
    private String imagen;

    @Column(name = "alt", length = 255)
    private String alt;

    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "contenido", nullable = false, columnDefinition = "json")
    private String contenido;

    @Column(name = "publicado", nullable = false)
    @Builder.Default
    private Boolean publicado = Boolean.TRUE;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
