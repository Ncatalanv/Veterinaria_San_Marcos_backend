package com.frontendlovers.sanmarcos.entity;

import com.frontendlovers.sanmarcos.entity.enums.TipoCategoria;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Entity 
@Table(name = "categorias", uniqueConstraints = {
    @UniqueConstraint(name = "uk_categorias_nombre_tipo", columnNames = {"nombre", "tipo"})
})
@Getter 
@Setter 
@NoArgsConstructor 
@AllArgsConstructor 
@Builder 
public class Categoria {

    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre", nullable = false, length = 80)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 20)
    private TipoCategoria tipo;

    @Column(name = "descripcion", length = 255)
    private String descripcion;

    @OneToMany(mappedBy = "categoria")
    @Builder.Default
    private List<Producto> productos = new ArrayList<>();
    
    @OneToMany(mappedBy = "categoria")
    @Builder.Default
    private List<Servicio> servicios = new ArrayList<>();

    @CreationTimestamp 
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp 
    @Column (name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

}
