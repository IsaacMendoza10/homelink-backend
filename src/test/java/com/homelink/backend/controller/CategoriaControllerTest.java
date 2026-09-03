package com.homelink.backend.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// Prueba de la capa Controller usando MockMvc, sobre el flujo real de
// categorias (listado publico + alta), tal como pide el Paso 3 del taller.
@SpringBootTest
@AutoConfigureMockMvc
class CategoriaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void listarCategorias_devuelveLaVistaConElModeloPoblado() throws Exception {
        mockMvc.perform(get("/categorias"))
                .andExpect(status().isOk())
                .andExpect(view().name("categorias/list"))
                .andExpect(model().attributeExists("categorias"));
    }

    @Test
    void crearCategoria_conDatosValidos_redirigeAlListado() throws Exception {
        mockMvc.perform(post("/categorias/nueva")
                        .param("nombre", "Jardineria-" + System.nanoTime())
                        .param("descripcion", "Mantenimiento de jardines y zonas verdes"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/categorias"));
    }
}
