package com.homelink.backend.controller;

import com.homelink.backend.model.EstadoAprobacion;
import com.homelink.backend.service.TrabajadorService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

// El panel de administracion ya esta protegido: SecurityConfig exige el rol
// ADMINISTRADOR para todo /admin/**, asi que si se llega hasta aqui es porque
// Spring Security ya autentico y autorizo al usuario. No hace falta repetir
// la verificacion a mano en el controlador.
@Controller
@RequestMapping("/admin")
public class AdminController {

    private final TrabajadorService trabajadorService;

    public AdminController(TrabajadorService trabajadorService) {
        this.trabajadorService = trabajadorService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("pendientes", trabajadorService.listarPendientesAprobacion());
        return "admin/dashboard";
    }

    @PostMapping("/trabajadores/{perfilId}/aprobar")
    public String aprobar(@PathVariable Long perfilId) {
        trabajadorService.cambiarEstadoAprobacion(perfilId, EstadoAprobacion.APROBADO);
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/trabajadores/{perfilId}/rechazar")
    public String rechazar(@PathVariable Long perfilId) {
        trabajadorService.cambiarEstadoAprobacion(perfilId, EstadoAprobacion.RECHAZADO);
        return "redirect:/admin/dashboard";
    }
}
