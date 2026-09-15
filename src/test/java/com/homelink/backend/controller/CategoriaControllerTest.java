package com.homelink.backend.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// Prueba de la capa Controller usando MockMvc, sobre el flujo real de
// categorias (listado publico + alta), y ahora tambien sobre las reglas de
// Spring Security que protegen la creacion de categorias.
@SpringBootTest
@AutoConfigureMockMvc
class CategoriaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void listarCategorias_esPublico_devuelveLaVistaConElModeloPoblado() throws Exception {
        mockMvc.perform(get("/categorias"))
                .andExpect(status().isOk())
                .andExpect(view().name("categorias/list"))
                .andExpect(model().attributeExists("categorias"));
    }

    @Test
    @WithMockUser(username = "admin@homelink.com", roles = "ADMINISTRADOR")
    void crearCategoria_comoAdministrador_redirigeAlListado() throws Exception {
        mockMvc.perform(post("/categorias/nueva")
                        .with(csrf())
                        .param("nombre", "Jardineria-" + System.nanoTime())
                        .param("descripcion", "Mantenimiento de jardines y zonas verdes"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/categorias"));
    }

    @Test
    void crearCategoria_sinAutenticarse_noLlegaAlListadoSinoAlLogin() throws Exception {
        // crear categorias con solo conocer la URL POST /categorias/nueva.
        // Antes de proteger este endpoint con Spring Security, cualquiera podia
        mockMvc.perform(post("/categorias/nueva")
                        .with(csrf())
                        .accept(MediaType.TEXT_HTML)
                        .param("nombre", "Categoria-no-autorizada-" + System.nanoTime())
                        .param("descripcion", "No deberia poder crearse sin iniciar sesion"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @WithMockUser(username = "cliente@homelink.com", roles = "CLIENTE")
    void crearCategoria_comoCliente_esRechazadaPorFaltaDeRol() throws Exception {
        mockMvc.perform(post("/categorias/nueva")
                        .with(csrf())
                        .param("nombre", "Categoria-cliente-" + System.nanoTime())
                        .param("descripcion", "Un cliente no deberia poder crear categorias"))
                .andExpect(status().isForbidden());
    }
}
