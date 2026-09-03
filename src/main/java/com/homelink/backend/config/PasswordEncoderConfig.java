package com.homelink.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

// Nota: solo se usa spring-security-crypto (BCrypt) para no complicar el
// arranque del proyecto con la configuracion completa de Spring Security.
// Esto significa que las contrasenas SI quedan hasheadas, pero los endpoints
// no estan protegidos por un filtro de autenticacion/autorizacion todavia.
@Configuration
public class PasswordEncoderConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
