package com.frontendlovers.sanmarcos.dto;

import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record PostRequest(
        @NotBlank(message = "El slug es obligatorio")
        @Size(max = 150, message = "El slug no puede superar 150 caracteres")
        String slug,

        @NotBlank(message = "El t��tulo es obligatorio")
        @Size(max = 200, message = "El t��tulo no puede superar 200 caracteres")
        String titulo,

        @Size(max = 80)
        String categoria,

        @Size(max = 500)
        String extracto,

        @Size(max = 255)
        String imagen,

        @Size(max = 255)
        String alt,

        @NotNull(message = "La fecha es obligatoria")
        LocalDate fecha,

        @NotNull(message = "El contenido es obligatorio")
        @Schema(description = "Contenido del post en formato JSON (arreglo de bloques)", example = "[{\"type\":\"paragraph\",\"text\":\"Texto...\"}]")
        JsonNode contenido,

        Boolean publicado
) {
}
