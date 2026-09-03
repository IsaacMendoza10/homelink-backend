package com.homelink.backend.controller;

import com.homelink.backend.model.Categoria;
import com.homelink.backend.model.PerfilTrabajador;
import com.homelink.backend.service.CategoriaService;
import com.homelink.backend.service.TrabajadorService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@Controller
@RequestMapping("/perfil")
public class PerfilController {

    private final TrabajadorService trabajadorService;
    private final CategoriaService categoriaService;

    public PerfilController(TrabajadorService trabajadorService, CategoriaService categoriaService) {
        this.trabajadorService = trabajadorService;
        this.categoriaService = categoriaService;
    }

    @GetMapping
    public String verPerfil(HttpSession session, Model model) {
        Long usuarioId = (Long) session.getAttribute("usuarioId");
        if (usuarioId == null) {
            return "redirect:/login";
        }
        PerfilTrabajador perfil = trabajadorService.buscarPorUsuarioId(usuarioId).orElse(null);
        model.addAttribute("perfil", perfil);
        model.addAttribute("categorias", categoriaService.listarTodas());
        return "perfil/ver";
    }

    @PostMapping("/actualizar")
    public String actualizarPerfil(HttpSession session,
                                    @RequestParam Long categoriaId,
                                    @RequestParam String zonaCobertura,
                                    @RequestParam String descripcion,
                                    @RequestParam BigDecimal tarifaReferencial,
                                    @RequestParam(required = false) String documentoUrl) {
        Long usuarioId = (Long) session.getAttribute("usuarioId");
        if (usuarioId == null) {
            return "redirect:/login";
        }
        PerfilTrabajador perfil = trabajadorService.buscarPorUsuarioId(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Perfil no encontrado"));
        Categoria categoria = categoriaService.buscarPorId(categoriaId)
                .orElseThrow(() -> new IllegalArgumentException("Categoria no encontrada"));
        perfil.setCategoria(categoria);
        perfil.setZonaCobertura(zonaCobertura);
        perfil.setDescripcion(descripcion);
        perfil.setTarifaReferencial(tarifaReferencial);
        perfil.setDocumentoUrl(documentoUrl);
        trabajadorService.actualizarPerfil(perfil);
        return "redirect:/perfil";
    }
}
