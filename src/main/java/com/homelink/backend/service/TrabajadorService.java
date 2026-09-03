package com.homelink.backend.service;

import com.homelink.backend.model.EstadoAprobacion;
import com.homelink.backend.model.PerfilTrabajador;
import com.homelink.backend.model.RolUsuario;
import com.homelink.backend.model.Usuario;
import com.homelink.backend.repository.PerfilTrabajadorRepository;
import com.homelink.backend.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TrabajadorService {

    private final UsuarioRepository usuarioRepository;
    private final PerfilTrabajadorRepository perfilTrabajadorRepository;
    private final PasswordEncoder passwordEncoder;

    public TrabajadorService(UsuarioRepository usuarioRepository,
                              PerfilTrabajadorRepository perfilTrabajadorRepository,
                              PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.perfilTrabajadorRepository = perfilTrabajadorRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public PerfilTrabajador registrarTrabajador(String nombre, String email, String password, String telefono) {
        if (usuarioRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Ya existe una cuenta registrada con ese correo.");
        }
        Usuario usuario = new Usuario();
        usuario.setNombre(nombre);
        usuario.setEmail(email);
        usuario.setPassword(passwordEncoder.encode(password));
        usuario.setTelefono(telefono);
        usuario.setRol(RolUsuario.TRABAJADOR);
        usuario = usuarioRepository.save(usuario);

        PerfilTrabajador perfil = new PerfilTrabajador();
        perfil.setUsuario(usuario);
        perfil.setEstadoAprobacion(EstadoAprobacion.PENDIENTE);
        return perfilTrabajadorRepository.save(perfil);
    }

    public Optional<PerfilTrabajador> buscarPorUsuarioId(Long usuarioId) {
        return perfilTrabajadorRepository.findByUsuarioId(usuarioId);
    }

    public PerfilTrabajador actualizarPerfil(PerfilTrabajador perfil) {
        return perfilTrabajadorRepository.save(perfil);
    }

    public List<PerfilTrabajador> listarPendientesAprobacion() {
        return perfilTrabajadorRepository.findByEstadoAprobacion(EstadoAprobacion.PENDIENTE);
    }

    public List<PerfilTrabajador> buscarDisponiblesPorCategoria(Long categoriaId) {
        return perfilTrabajadorRepository.findByCategoriaIdAndEstadoAprobacion(categoriaId, EstadoAprobacion.APROBADO);
    }

    public PerfilTrabajador cambiarEstadoAprobacion(Long perfilId, EstadoAprobacion estado) {
        PerfilTrabajador perfil = perfilTrabajadorRepository.findById(perfilId)
                .orElseThrow(() -> new IllegalArgumentException("Perfil de trabajador no encontrado: " + perfilId));
        perfil.setEstadoAprobacion(estado);
        return perfilTrabajadorRepository.save(perfil);
    }
}
