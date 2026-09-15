package com.homelink.backend.controller;

import com.homelink.backend.model.Categoria;
import com.homelink.backend.service.CategoriaService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

// El listado (GET) sigue siendo publico a proposito (un cliente debe poder
// ver las categorias sin haber iniciado sesion), pero crear y eliminar
// categorias (POST) ahora exige el rol ADMINISTRADOR: la regla vive en
// SecurityConfig, no en este controlador.
@Controller
@RequestMapping("/categorias")
public class CategoriaController {

    private final CategoriaService categoriaService;

    public CategoriaController(CategoriaService categoriaService) {
        this.categoriaService = categoriaService;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("categorias", categoriaService.listarTodas());
        return "categorias/list";
    }

    @PostMapping("/nueva")
    public String crear(@RequestParam String nombre, @RequestParam String descripcion) {
        categoriaService.guardar(new Categoria(nombre, descripcion));
        return "redirect:/categorias";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable Long id) {
        categoriaService.eliminar(id);
        return "redirect:/categorias";
    }
}
