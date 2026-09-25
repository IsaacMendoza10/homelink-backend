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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
        if (perfil != null) {
            model.addAttribute("misCategorias", trabajadorService.listarCategoriasDe(perfil.getId()));
        }
        return "perfil/ver";
    }

    @PostMapping("/actualizar")
    public String actualizarPerfil(@AuthenticationPrincipal CustomUserDetails principal,
                                    @RequestParam String zonaCobertura,
                                    @RequestParam String descripcion,
                                    @RequestParam(required = false) String documentoUrl) {
        Long usuarioId = principal.getUsuario().getId();
        PerfilTrabajador perfil = trabajadorService.buscarPorUsuarioId(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Perfil no encontrado"));
        perfil.setZonaCobertura(zonaCobertura);
        perfil.setDescripcion(descripcion);
        perfil.setDocumentoUrl(documentoUrl);
        trabajadorService.actualizarPerfil(perfil);
        return "redirect:/perfil";
    }

    // Agregar una categoria mas al perfil (con su propia tarifa). Si el
    // trabajador ya la ofrece, se avisa con un mensaje en vez de duplicarla.
    @PostMapping("/categorias/agregar")
    public String agregarCategoria(@AuthenticationPrincipal CustomUserDetails principal,
                                    @RequestParam Long categoriaId,
                                    @RequestParam BigDecimal tarifa,
                                    RedirectAttributes redirectAttributes) {
        Long usuarioId = principal.getUsuario().getId();
        PerfilTrabajador perfil = trabajadorService.buscarPorUsuarioId(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Perfil no encontrado"));
        Categoria categoria = categoriaService.buscarPorId(categoriaId)
                .orElseThrow(() -> new IllegalArgumentException("Categoria no encontrada"));
        try {
            trabajadorService.agregarCategoria(perfil, categoria, tarifa);
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/perfil";
    }

    @PostMapping("/categorias/{id}/eliminar")
    public String eliminarCategoria(@AuthenticationPrincipal CustomUserDetails principal, @PathVariable Long id) {
        Long usuarioId = principal.getUsuario().getId();
        PerfilTrabajador perfil = trabajadorService.buscarPorUsuarioId(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Perfil no encontrado"));
        trabajadorService.quitarCategoria(perfil, id);
        return "redirect:/perfil";
    }
}
