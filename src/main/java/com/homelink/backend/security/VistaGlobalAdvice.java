package com.homelink.backend.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

// Expone el rol del usuario autenticado (o null si nadie ha iniciado sesion)
// a TODAS las vistas, sin que cada controlador tenga que agregarlo a mano.
// Se agrego al endurecer el acceso a /perfil/** (ver SecurityConfig): antes,
// fragments/nav.html mostraba el enlace "Mi perfil" a cualquiera sin importar
// su rol, y ahora un CLIENTE que le diera clic se habria encontrado con un
// error 403. Con "rolActual" disponible en todas las plantillas, el menu
// puede mostrar solo los enlaces que le corresponden a cada quien.
@ControllerAdvice
public class VistaGlobalAdvice {

    @ModelAttribute("rolActual")
    public String rolActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails principal) {
            return principal.getUsuario().getRol().name();
        }
        return null;
    }
}
