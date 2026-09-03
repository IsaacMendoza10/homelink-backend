package com.homelink.backend.service;

import com.homelink.backend.model.Usuario;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

// Prueba de la capa Service (logica de negocio real de HomeLink), corriendo
// contra la base de datos en memoria H2 configurada en src/test/resources.
@SpringBootTest
class UsuarioServiceTest {

    @Autowired
    private UsuarioService usuarioService;

    private String emailUnico() {
        return "cliente-" + UUID.randomUUID() + "@homelink.test";
    }

    @Test
    void registrarCliente_almacenaLaContrasenaHasheadaNoEnTextoPlano() {
        String email = emailUnico();
        Usuario usuario = usuarioService.registrarCliente(
                "Ana Torres", email, "clave123", "3000000000", "Calle 1 #2-3");

        assertNotNull(usuario.getId());
        assertNotEquals("clave123", usuario.getPassword(),
                "La contrasena nunca debe guardarse en texto plano");
    }

    @Test
    void registrarCliente_conEmailDuplicado_lanzaExcepcion() {
        String email = emailUnico();
        usuarioService.registrarCliente("Primer Registro", email, "clave123", "3000000000", "Calle 1");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                usuarioService.registrarCliente("Segundo Registro", email, "otraClave", "3000000001", "Calle 2"));
        assertTrue(ex.getMessage().toLowerCase().contains("correo"));
    }

    @Test
    void autenticar_conCredencialesCorrectas_devuelveElUsuario() {
        String email = emailUnico();
        usuarioService.registrarCliente("Carlos Ruiz", email, "miClaveSegura", "3000000002", "Calle 3");

        Optional<Usuario> resultado = usuarioService.autenticar(email, "miClaveSegura");

        assertTrue(resultado.isPresent());
        assertEquals(email, resultado.get().getEmail());
    }

    @Test
    void autenticar_conPasswordIncorrecta_noDevuelveUsuario() {
        String email = emailUnico();
        usuarioService.registrarCliente("Diana Paez", email, "claveCorrecta", "3000000003", "Calle 4");

        Optional<Usuario> resultado = usuarioService.autenticar(email, "claveIncorrecta");

        assertTrue(resultado.isEmpty());
    }
}
