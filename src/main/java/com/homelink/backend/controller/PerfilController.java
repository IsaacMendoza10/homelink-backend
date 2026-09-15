package com.homelink.backend.controller;

import com.homelink.backend.model.Categoria;
import com.homelink.backend.model.PerfilTrabajador;
import com.homelink.backend.security.CustomUserDetails;
import com.homelink.backend.service.CategoriaService;
import com.homelink.backend.service.TrabajadorService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

// Antes el usuario autenticado se leia a mano de la HttpSession
// ("usuarioId"), que dependia de que AuthController la hubiera poblado en el
// login manual. Ahora Spring Security ya garantiza que solo se llega aqui
// autenticado (ver SecurityConfig) y nos entrega directamente el Usuario real
// a traves de CustomUserDetails, sin volver a consultar la base de datos.
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
    public String verPerfil(@AuthenticationPrincipal CustomUserDetails principal, Model model) {
        Long usuarioId = principal.getUsuario().getId();
        PerfilTrabajador perfil = trabajadorService.buscarPorUsuarioId(usuarioId).orElse(null);
        model.addAttribute("perfil", perfil);
        model.addAttribute("categorias", categoriaService.listarTodas());
        return "perfil/ver";
    }

    @PostMapping("/actualizar")
    public String actualizarPerfil(@AuthenticationPrincipal CustomUserDetails principal,
                                    @RequestParam Long categoriaId,
                                    @RequestParam String zonaCobertura,
                                    @RequestParam String descripcion,
                                    @RequestParam BigDecimal tarifaReferencial,
                                    @RequestParam(required = false) String documentoUrl) {
        Long usuarioId = principal.getUsuario().getId();
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
