package com.homelink.backend.config;

import com.homelink.backend.model.RolUsuario;
import com.homelink.backend.security.CustomUserDetails;
import com.homelink.backend.security.CustomUserDetailsService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.LogoutConfigurer;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.http.HttpStatus;

// Configuracion central de Spring Security para HomeLink.
//
// Antes de esto, ningun endpoint estaba realmente protegido a nivel de
// framework (ver los comentarios que había en AuthController, AdminController
// y CategoriaController): la sesion se manejaba a mano y cualquiera que
// conociera una URL de administrador podia usarla sin haber iniciado sesion.
// Ahora las reglas de acceso quedan centralizadas aqui, en un solo lugar.
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;

    public SecurityConfig(CustomUserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                // Publico: paginas informativas, autenticacion/registro, recursos estaticos
                // y los endpoints de solo lectura que cualquier visitante debe poder usar
                // (buscar trabajadores/categorias es parte del flujo normal de un cliente
                // que todavia no se ha registrado).
                .requestMatchers("/", "/login", "/registro", "/registro-trabajador").permitAll()
                .requestMatchers("/css/**", "/js/**", "/images/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/categorias", "/trabajadores").permitAll()
                .requestMatchers("/api/estado", "/api/trabajadores/buscar").permitAll()
                .requestMatchers("/actuator/health").permitAll()

                // Panel de administrador y gestion de categorias (crear/eliminar):
                // antes cualquiera podia entrar aqui con solo conocer la URL.
                .requestMatchers("/admin/**").hasRole(RolUsuario.ADMINISTRADOR.name())
                .requestMatchers(HttpMethod.POST, "/categorias/nueva", "/categorias/*/eliminar")
                        .hasRole(RolUsuario.ADMINISTRADOR.name())
                .requestMatchers("/actuator/**").hasRole(RolUsuario.ADMINISTRADOR.name())

                // /perfil/**: solo tiene sentido para una cuenta TRABAJADOR (es su perfil
                // profesional). Antes bastaba con estar autenticado con cualquier rol, asi
                // que un CLIENTE podia entrar aunque no tuviera nada que hacer ahi.
                .requestMatchers("/perfil/**").hasRole(RolUsuario.TRABAJADOR.name())

                // /solicitudes/**: cualquier usuario autenticado (cliente o trabajador);
                // cada controlador ya distingue el flujo por rol.
                .requestMatchers("/solicitudes/**").authenticated()

                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .usernameParameter("email")
                .passwordParameter("password")
                .successHandler(loginSuccessHandler())
                .failureUrl("/login?error")
                .permitAll()
            )
            .logout(this::configurarLogout)
            // Si alguien sin sesion pide un endpoint JSON protegido (por ejemplo /actuator/**),
            // es mejor devolver 401 que redirigir a una pagina HTML de login.
            .exceptionHandling(ex -> ex
                .defaultAuthenticationEntryPointFor(
                        new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
                        new AntPathRequestMatcher("/actuator/**"))
            );

        return http.build();
    }

    private void configurarLogout(LogoutConfigurer<HttpSecurity> logout) {
        logout
            // El enlace "Cerrar sesion" del menu es un simple <a href="/logout"> (GET),
            // no un formulario, asi que se permite ese metodo solo para esta ruta puntual.
            .logoutRequestMatcher(new AntPathRequestMatcher("/logout", "GET"))
            .logoutSuccessUrl("/")
            .invalidateHttpSession(true)
            .deleteCookies("JSESSIONID");
    }

    // Redirige a cada rol a la pantalla que ya tenia sentido para el en el flujo
    // original de AuthController (administrador -> dashboard, trabajador -> su
    // perfil, cliente -> sus solicitudes).
    private org.springframework.security.web.authentication.AuthenticationSuccessHandler loginSuccessHandler() {
        return (HttpServletRequest request, HttpServletResponse response, org.springframework.security.core.Authentication authentication) -> {
            CustomUserDetails principal = (CustomUserDetails) authentication.getPrincipal();
            String destino = switch (principal.getUsuario().getRol()) {
                case ADMINISTRADOR -> "/admin/dashboard";
                case TRABAJADOR -> "/perfil";
                case CLIENTE -> "/solicitudes";
            };
            response.sendRedirect(request.getContextPath() + destino);
        };
    }
}
