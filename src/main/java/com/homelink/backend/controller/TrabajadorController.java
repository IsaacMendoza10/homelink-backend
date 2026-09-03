package com.homelink.backend.controller;

import com.homelink.backend.model.Categoria;
import com.homelink.backend.service.CategoriaService;
import com.homelink.backend.service.TrabajadorService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/trabajadores")
public class TrabajadorController {

    private final TrabajadorService trabajadorService;
    private final CategoriaService categoriaService;

    public TrabajadorController(TrabajadorService trabajadorService, CategoriaService categoriaService) {
        this.trabajadorService = trabajadorService;
        this.categoriaService = categoriaService;
    }

    @GetMapping
    public String buscar(@RequestParam(required = false) Long categoriaId, Model model) {
        model.addAttribute("categorias", categoriaService.listarTodas());
        if (categoriaId != null) {
            model.addAttribute("trabajadores", trabajadorService.buscarDisponiblesPorCategoria(categoriaId));
        }
        return "trabajadores/list";
    }
}
