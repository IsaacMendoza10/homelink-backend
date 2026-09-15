package com.homelink.backend.security;

import com.homelink.backend.model.Usuario;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

// Adapta nuestra entidad Usuario (ya existente, con rol y password hasheada
// con BCrypt) al contrato UserDetails que necesita Spring Security. No se
// duplica informacion: esta clase solo envuelve al Usuario real, asi que los
// controladores pueden pedir el Usuario completo (id, nombre, rol, etc.)
// a traves de getUsuario() sin tener que volver a consultar la base de datos.
public class CustomUserDetails implements UserDetails {

    private final Usuario usuario;

    public CustomUserDetails(Usuario usuario) {
        this.usuario = usuario;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Spring Security espera el prefijo "ROLE_" para que hasRole("ADMINISTRADOR")
        // funcione en las reglas de SecurityConfig.
        return List.of(new SimpleGrantedAuthority("ROLE_" + usuario.getRol().name()));
    }

    @Override
    public String getPassword() {
        return usuario.getPassword();
    }

    @Override
    public String getUsername() {
        return usuario.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
