package com.homelink.backend.controller;

import com.homelink.backend.service.TrabajadorService;
import com.homelink.backend.service.UsuarioService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

// El login y el logout ya no se manejan aqui: Spring Security intercepta
// POST /login (ver SecurityConfig.formLogin, con usernameParameter("email")
// para que coincida con el campo del formulario existente) y GET /logout
// directamente en el filtro, antes de que la peticion llegue a un controlador.
// Este controlador solo sirve las paginas y procesa el registro de cuentas nuevas.
@Controller
public class AuthController {

    private final UsuarioService usuarioService;
    private final TrabajadorService trabajadorService;

    public AuthController(UsuarioService usuarioService, TrabajadorService trabajadorService) {
        this.usuarioService = usuarioService;
        this.trabajadorService = trabajadorService;
    }

    @GetMapping("/login")
    public String loginForm() {
        return "auth/login";
    }

    @GetMapping("/registro")
    public String registroClienteForm() {
        return "auth/registro-cliente";
    }

    @PostMapping("/registro")
    public String registroCliente(@RequestParam String nombre, @RequestParam String email,
                                   @RequestParam String password, @RequestParam String telefono,
                                   @RequestParam String direccion, Model model) {
        try {
            usuarioService.registrarCliente(nombre, email, password, telefono, direccion);
            return "redirect:/login";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "auth/registro-cliente";
        }
    }

    @GetMapping("/registro-trabajador")
    public String registroTrabajadorForm() {
        return "auth/registro-trabajador";
    }

    @PostMapping("/registro-trabajador")
    public String registroTrabajador(@RequestParam String nombre, @RequestParam String email,
                                      @RequestParam String password, @RequestParam String telefono,
                                      Model model) {
        try {
            trabajadorService.registrarTrabajador(nombre, email, password, telefono);
            return "redirect:/login";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "auth/registro-trabajador";
        }
    }
}
