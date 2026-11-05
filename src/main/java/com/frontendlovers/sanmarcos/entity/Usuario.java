package com.frontendlovers.sanmarcos.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "usuarios")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 15)
    private String run; // Único, ej: "12345678-9"

    @Column(nullable = false, length = 80)
    private String nombre;

    @Column(nullable = false, length = 80)
    private String apellido;

    @Column(nullable = false, unique = true, length = 120)
    private String email; // Único

    @Column(name = "password_hash", nullable = false, length = 255)
    private String password;

    @Column(length = 20)
    private String telefono;

    private LocalDate fechaNacimiento;

    @Column(length = 255)
    private String direccion;

    @Column(length = 80)
    private String region;

    @Column(length = 80)
    private String comuna;

    // Relación ManyToOne con la entidad Rol
    @ManyToOne
    @JoinColumn(name = "rol_id", nullable = false)
    private Rol rol;
}