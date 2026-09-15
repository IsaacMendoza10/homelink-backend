package com.homelink.backend.controller;

import com.homelink.backend.model.*;
import com.homelink.backend.security.CustomUserDetails;
import com.homelink.backend.service.CategoriaService;
import com.homelink.backend.service.SolicitudService;
import com.homelink.backend.service.UsuarioService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// El id y el rol del usuario ya no se leen de la HttpSession: llegan
// garantizados por Spring Security a traves de CustomUserDetails (ver
// SecurityConfig, que exige autenticacion para todo /solicitudes/**).
@Controller
@RequestMapping("/solicitudes")
public class SolicitudController {

    private final SolicitudService solicitudService;
    private final CategoriaService categoriaService;
    private final UsuarioService usuarioService;

    public SolicitudController(SolicitudService solicitudService, CategoriaService categoriaService,
                                UsuarioService usuarioService) {
        this.solicitudService = solicitudService;
        this.categoriaService = categoriaService;
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public String listar(@AuthenticationPrincipal CustomUserDetails principal, Model model) {
        Long usuarioId = principal.getUsuario().getId();
        String rol = principal.getUsuario().getRol().name();
        List<Solicitud> solicitudes = "TRABAJADOR".equals(rol)
                ? solicitudService.listarPorTrabajador(usuarioId)
                : solicitudService.listarPorCliente(usuarioId);
        model.addAttribute("solicitudes", solicitudes);
        model.addAttribute("rol", rol);
        return "solicitudes/list";
    }

    @GetMapping("/nueva")
    public String nuevaForm(Model model) {
        model.addAttribute("categorias", categoriaService.listarTodas());
        return "solicitudes/form";
    }

    @PostMapping("/nueva")
    public String crear(@AuthenticationPrincipal CustomUserDetails principal, @RequestParam Long categoriaId,
                         @RequestParam String descripcion, @RequestParam String direccion) {
        Usuario cliente = usuarioService.buscarPorId(principal.getUsuario().getId())
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        Categoria categoria = categoriaService.buscarPorId(categoriaId)
                .orElseThrow(() -> new IllegalArgumentException("Categoria no encontrada"));
        solicitudService.crear(cliente, categoria, descripcion, direccion);
        return "redirect:/solicitudes";
    }

    @PostMapping("/{id}/aceptar")
    public String aceptar(@AuthenticationPrincipal CustomUserDetails principal, @PathVariable Long id) {
        Usuario trabajador = usuarioService.buscarPorId(principal.getUsuario().getId())
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        solicitudService.aceptar(id, trabajador);
        return "redirect:/solicitudes";
    }

    @PostMapping("/{id}/finalizar")
    public String finalizar(@PathVariable Long id) {
        solicitudService.finalizar(id);
        return "redirect:/solicitudes";
    }

    @PostMapping("/{id}/cancelar")
    public String cancelar(@PathVariable Long id) {
        solicitudService.cancelar(id);
        return "redirect:/solicitudes";
    }

    @PostMapping("/{id}/calificar")
    public String calificar(@PathVariable Long id, @RequestParam int puntuacion,
                             @RequestParam(required = false) String comentario) {
        solicitudService.calificar(id, puntuacion, comentario);
        return "redirect:/solicitudes";
    }
}
