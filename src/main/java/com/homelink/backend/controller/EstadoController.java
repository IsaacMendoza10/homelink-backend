package com.homelink.backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

// Endpoint pequeno, agregado en una rama de funcionalidad, para demostrar
// el flujo de Pull Request protegido por el pipeline (Paso 5 del taller de CI/CD).
@RestController
public class EstadoController {

    @GetMapping("/api/estado")
    public Map<String, Object> estado() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("aplicacion", "HomeLink");
        body.put("estado", "UP");
        body.put("timestamp", LocalDateTime.now().toString());
        return body;
    }
}
