package com.frontendlovers.sanmarcos.dto;

import com.frontendlovers.sanmarcos.entity.enums.TipoEntrega;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

// Datos que envía el frontend al hacer checkout
public record OrdenRequest(
        @NotBlank String direccionEnvio,
        @NotNull TipoEntrega tipoEntrega,
        String instrucciones,
        @Size(max = 80) String contactoNombre,
        @Size(max = 80) String contactoApellido,
        @Email @Size(max = 120) String contactoEmail,
        @Size(max = 20) String contactoTelefono,
        @NotEmpty @Valid List<OrdenItemRequest> items
) {}
