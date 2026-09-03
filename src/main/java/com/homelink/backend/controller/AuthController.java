package com.homelink.backend.controller;

import com.homelink.backend.model.Usuario;
import com.homelink.backend.service.TrabajadorService;
import com.homelink.backend.service.UsuarioService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

// Autenticacion basada en sesion HTTP "hecha a mano": no hay Spring Security
// configurado, por lo que ningun endpoint (ni siquiera /admin/**) esta
// realmente protegido a nivel de framework; la comprobacion de sesion queda
// a criterio de cada controlador (o directamente no se hace, como en varios
// GET de este proyecto).
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

    @PostMapping("/login")
    public String login(@RequestParam String email, @RequestParam String password,
                         HttpSession session, Model model) {
        Optional<Usuario> usuario = usuarioService.autenticar(email, password);
        if (usuario.isEmpty()) {
            model.addAttribute("error", "Correo o contrasena incorrectos.");
            return "auth/login";
        }
        session.setAttribute("usuarioId", usuario.get().getId());
        session.setAttribute("rol", usuario.get().getRol().name());
        session.setAttribute("nombre", usuario.get().getNombre());
        return switch (usuario.get().getRol()) {
            case ADMINISTRADOR -> "redirect:/admin/dashboard";
            case TRABAJADOR -> "redirect:/perfil";
            default -> "redirect:/solicitudes";
        };
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
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
