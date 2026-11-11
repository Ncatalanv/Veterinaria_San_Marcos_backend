package com.frontendlovers.sanmarcos;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

//Controlador exclusivo para pruebas

@RestController
public class TestAdminController {

    @GetMapping("/api/v1/test-solo-admin")
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, String> soloAdmin() {
        return Map.of("mensaje", "ok");
    }
}