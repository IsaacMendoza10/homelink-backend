package com.homelink.backend.service;

import com.homelink.backend.model.Categoria;
import com.homelink.backend.model.EstadoAprobacion;
import com.homelink.backend.model.PerfilTrabajador;
import com.homelink.backend.model.RolUsuario;
import com.homelink.backend.model.TrabajadorCategoria;
import com.homelink.backend.model.Usuario;
import com.homelink.backend.repository.PerfilTrabajadorRepository;
import com.homelink.backend.repository.TrabajadorCategoriaRepository;
import com.homelink.backend.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class TrabajadorService {

    private final UsuarioRepository usuarioRepository;
    private final PerfilTrabajadorRepository perfilTrabajadorRepository;
    private final TrabajadorCategoriaRepository trabajadorCategoriaRepository;
    private final PasswordEncoder passwordEncoder;

    public TrabajadorService(UsuarioRepository usuarioRepository,
                              PerfilTrabajadorRepository perfilTrabajadorRepository,
                              TrabajadorCategoriaRepository trabajadorCategoriaRepository,
                              PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.perfilTrabajadorRepository = perfilTrabajadorRepository;
        this.trabajadorCategoriaRepository = trabajadorCategoriaRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // El registro sigue creando un perfil "vacio" (sin categorias todavia): el
    // trabajador las agrega despues desde "Gestionar mi perfil", una vez
    // autenticado, para no alargar el formulario de registro.
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

    public PerfilTrabajador cambiarEstadoAprobacion(Long perfilId, EstadoAprobacion estado) {
        PerfilTrabajador perfil = perfilTrabajadorRepository.findById(perfilId)
                .orElseThrow(() -> new IllegalArgumentException("Perfil de trabajador no encontrado: " + perfilId));
        perfil.setEstadoAprobacion(estado);
        return perfilTrabajadorRepository.save(perfil);
    }

    // Categorias y tarifas que ofrece un trabajador (relacion N a N: puede tener
    // varias, cada una con su propia tarifa).
    public List<TrabajadorCategoria> listarCategoriasDe(Long perfilId) {
        return trabajadorCategoriaRepository.findByPerfilTrabajadorId(perfilId);
    }

    // Trabajadores aprobados que ofrecen una categoria dada. Cada resultado trae
    // la tarifa especifica de ESA categoria (no una tarifa generica del perfil),
    // porque el mismo trabajador puede cobrar distinto segun el oficio.
    public List<TrabajadorCategoria> buscarDisponiblesPorCategoria(Long categoriaId) {
        return trabajadorCategoriaRepository.findByCategoriaIdAndPerfilTrabajadorEstadoAprobacion(
                categoriaId, EstadoAprobacion.APROBADO);
    }

    public TrabajadorCategoria agregarCategoria(PerfilTrabajador perfil, Categoria categoria, BigDecimal tarifa) {
        if (trabajadorCategoriaRepository.existsByPerfilTrabajadorIdAndCategoriaId(perfil.getId(), categoria.getId())) {
            throw new IllegalStateException("Ya ofreces esta categoria.");
        }
        TrabajadorCategoria tc = new TrabajadorCategoria();
        tc.setPerfilTrabajador(perfil);
        tc.setCategoria(categoria);
        tc.setTarifa(tarifa);
        return trabajadorCategoriaRepository.save(tc);
    }

    public void quitarCategoria(PerfilTrabajador perfil, Long trabajadorCategoriaId) {
        TrabajadorCategoria tc = trabajadorCategoriaRepository.findById(trabajadorCategoriaId)
                .orElseThrow(() -> new IllegalArgumentException("Categoria no encontrada."));
        if (!tc.getPerfilTrabajador().getId().equals(perfil.getId())) {
            throw new IllegalArgumentException("Esta categoria no pertenece a tu perfil.");
        }
        trabajadorCategoriaRepository.delete(tc);
    }
}
