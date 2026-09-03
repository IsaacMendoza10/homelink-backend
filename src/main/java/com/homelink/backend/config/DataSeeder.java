package com.homelink.backend.config;

import com.homelink.backend.model.Categoria;
import com.homelink.backend.model.RolUsuario;
import com.homelink.backend.model.Usuario;
import com.homelink.backend.repository.CategoriaRepository;
import com.homelink.backend.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    private final CategoriaRepository categoriaRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(CategoriaRepository categoriaRepository,
                       UsuarioRepository usuarioRepository,
                       PasswordEncoder passwordEncoder) {
        this.categoriaRepository = categoriaRepository;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (categoriaRepository.count() == 0) {
            categoriaRepository.save(new Categoria("Plomeria", "Instalacion y reparacion de tuberias, grifos y sanitarios"));
            categoriaRepository.save(new Categoria("Electricidad", "Instalaciones electricas, cortos y mantenimiento"));
            categoriaRepository.save(new Categoria("Pintura", "Pintura de interiores y exteriores"));
            categoriaRepository.save(new Categoria("Cerrajeria", "Apertura de puertas, cambio de cerraduras y llaves"));
        }

        if (!usuarioRepository.existsByEmail("admin@homelink.com")) {
            Usuario admin = new Usuario();
            admin.setNombre("Administrador HomeLink");
            admin.setEmail("admin@homelink.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRol(RolUsuario.ADMINISTRADOR);
            usuarioRepository.save(admin);
        }
    }
}
