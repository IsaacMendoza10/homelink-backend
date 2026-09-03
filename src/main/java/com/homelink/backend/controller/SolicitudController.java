package com.homelink.backend.controller;

import com.homelink.backend.model.*;
import com.homelink.backend.service.CategoriaService;
import com.homelink.backend.service.SolicitudService;
import com.homelink.backend.service.UsuarioService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public String listar(HttpSession session, Model model) {
        Long usuarioId = (Long) session.getAttribute("usuarioId");
        String rol = (String) session.getAttribute("rol");
        if (usuarioId == null) {
            return "redirect:/login";
        }
        List<Solicitud> solicitudes = "TRABAJADOR".equals(rol)
                ? solicitudService.listarPorTrabajador(usuarioId)
                : solicitudService.listarPorCliente(usuarioId);
        model.addAttribute("solicitudes", solicitudes);
        model.addAttribute("rol", rol);
        return "solicitudes/list";
    }

    @GetMapping("/nueva")
    public String nuevaForm(HttpSession session, Model model) {
        if (session.getAttribute("usuarioId") == null) {
            return "redirect:/login";
        }
        model.addAttribute("categorias", categoriaService.listarTodas());
        return "solicitudes/form";
    }

    @PostMapping("/nueva")
    public String crear(HttpSession session, @RequestParam Long categoriaId,
                         @RequestParam String descripcion, @RequestParam String direccion) {
        Long usuarioId = (Long) session.getAttribute("usuarioId");
        if (usuarioId == null) {
            return "redirect:/login";
        }
        Usuario cliente = usuarioService.buscarPorId(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        Categoria categoria = categoriaService.buscarPorId(categoriaId)
                .orElseThrow(() -> new IllegalArgumentException("Categoria no encontrada"));
        solicitudService.crear(cliente, categoria, descripcion, direccion);
        return "redirect:/solicitudes";
    }

    @PostMapping("/{id}/aceptar")
    public String aceptar(HttpSession session, @PathVariable Long id) {
        Long usuarioId = (Long) session.getAttribute("usuarioId");
        Usuario trabajador = usuarioService.buscarPorId(usuarioId)
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
