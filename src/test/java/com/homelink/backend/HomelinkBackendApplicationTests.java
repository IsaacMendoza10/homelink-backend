package com.homelink.backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

// Unica prueba del proyecto: verifica que el contexto de Spring arranca.
// No hay pruebas de la capa Service (reglas de negocio) ni de los
// controladores. Es un buen ejemplo de "cobertura casi nula" para el
// Paso 8 del taller de analisis tecnico.
@SpringBootTest
class HomelinkBackendApplicationTests {

    @Test
    void contextLoads() {
    }

}
