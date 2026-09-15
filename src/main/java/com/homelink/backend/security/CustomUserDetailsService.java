package com.homelink.backend.security;

import com.homelink.backend.model.Usuario;
import com.homelink.backend.repository.UsuarioRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

// Punto de union entre Spring Security y nuestra tabla "usuarios": dado un
// email (lo usamos como "username"), busca el Usuario y lo envuelve en
// CustomUserDetails. La verificacion de la contrasena (BCrypt) la hace
// Spring Security automaticamente con el PasswordEncoder ya definido en
// PasswordEncoderConfig, no hay que compararla a mano aqui.
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public CustomUserDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("No existe una cuenta con ese correo: " + email));
        return new CustomUserDetails(usuario);
    }
}
