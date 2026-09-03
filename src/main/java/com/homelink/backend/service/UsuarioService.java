package com.homelink.backend.service;

import com.homelink.backend.model.RolUsuario;
import com.homelink.backend.model.Usuario;
import com.homelink.backend.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Usuario registrarCliente(String nombre, String email, String password, String telefono, String direccion) {
        if (usuarioRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Ya existe una cuenta registrada con ese correo.");
        }
        Usuario usuario = new Usuario();
        usuario.setNombre(nombre);
        usuario.setEmail(email);
        usuario.setPassword(passwordEncoder.encode(password));
        usuario.setTelefono(telefono);
        usuario.setDireccion(direccion);
        usuario.setRol(RolUsuario.CLIENTE);
        return usuarioRepository.save(usuario);
    }

    public Usuario registrarUsuarioBase(String nombre, String email, String password, String telefono, RolUsuario rol) {
        if (usuarioRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Ya existe una cuenta registrada con ese correo.");
        }
        Usuario usuario = new Usuario();
        usuario.setNombre(nombre);
        usuario.setEmail(email);
        usuario.setPassword(passwordEncoder.encode(password));
        usuario.setTelefono(telefono);
        usuario.setRol(rol);
        return usuarioRepository.save(usuario);
    }

    public Optional<Usuario> autenticar(String email, String password) {
        Optional<Usuario> usuario = usuarioRepository.findByEmail(email);
        if (usuario.isPresent() && passwordEncoder.matches(password, usuario.get().getPassword())) {
            return usuario;
        }
        return Optional.empty();
    }

    public Optional<Usuario> buscarPorId(Long id) {
        return usuarioRepository.findById(id);
    }

    public Optional<Usuario> buscarPorEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }
}
