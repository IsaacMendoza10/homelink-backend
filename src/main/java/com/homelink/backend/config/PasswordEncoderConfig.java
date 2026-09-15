package com.homelink.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

// Este bean lo usa tanto DataSeeder/UsuarioService (para hashear contrasenas)
// como SecurityConfig.authenticationProvider() (para verificarlas en el login),
// asi que solo hay una definicion de PasswordEncoder en todo el proyecto.
@Configuration
public class PasswordEncoderConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
