package com.frontendlovers.sanmarcos.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                // Configurar orígenes permitidos: localhost:5173 y tu dominio en GitHub Pages
                .allowedOriginPatterns(
                        "http://localhost:5173",
                        "https://*.github.io"
                )
                // Permitir métodos necesarios
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                // Permitir headers necesarios
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
