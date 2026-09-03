package com.homelink.backend.controller;

import com.homelink.backend.model.PerfilTrabajador;
import com.homelink.backend.service.TrabajadorService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// Endpoint JSON de ejemplo. Devuelve directamente la entidad PerfilTrabajador
// (que a su vez incluye la entidad Usuario completa, con el hash de la
// contrasena) en vez de un DTO. No hay manejo de errores especifico: un
// categoriaId inexistente simplemente devuelve una lista vacia, y cualquier
// excepcion inesperada produce el error 500 por defecto de Spring Boot.
@RestController
@RequestMapping("/api/trabajadores")
public class TrabajadorApiController {

    private final TrabajadorService trabajadorService;

    public TrabajadorApiController(TrabajadorService trabajadorService) {
        this.trabajadorService = trabajadorService;
    }

    @GetMapping("/buscar")
    public List<PerfilTrabajador> buscar(@RequestParam Long categoriaId) {
        return trabajadorService.buscarDisponiblesPorCategoria(categoriaId);
    }
}
