package com.frontendlovers.sanmarcos.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Veterinaria San Marcos - API Backend",
                version = "1.0.0",
                description = "Documentación oficial de los endpoints REST para la gestión de productos, servicios, usuarios y solicitudes médicas de Veterinaria San Marcos.",
                contact = @Contact(
                        name = "Frontend Lovers",
                        email = "contacto@sanmarcos.com"
                )
        ),
        servers = {
                @Server(url = "http://localhost:8080", description = "Servidor Local de Desarrollo")
        },
        security = {
                @SecurityRequirement(name = "bearerAuth")
        }
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "Ingresa tu token JWT para autenticarte en los endpoints protegidos."
)
public class OpenApiConfig {
}