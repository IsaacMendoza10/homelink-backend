package com.homelink.backend.service;

import com.homelink.backend.model.*;
import com.homelink.backend.repository.CalificacionRepository;
import com.homelink.backend.repository.SolicitudRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

// Pruebas unitarias de SolicitudService.iniciar: solo el trabajador asignado
// puede marcar una solicitud ACEPTADA como EN_PROCESO, y solo si la solicitud
// sigue en ese estado (no tiene sentido "iniciar" una que ya esta en proceso,
// finalizada, cancelada, etc.).
@ExtendWith(MockitoExtension.class)
class SolicitudServiceTest {

    @Mock
    private SolicitudRepository solicitudRepository;

    @Mock
    private CalificacionRepository calificacionRepository;

    private SolicitudService solicitudService;

    private Usuario trabajador;
    private Solicitud solicitud;

    @BeforeEach
    void setUp() {
        solicitudService = new SolicitudService(solicitudRepository, calificacionRepository);

        trabajador = new Usuario();
        trabajador.setId(2L);
        trabajador.setRol(RolUsuario.TRABAJADOR);

        solicitud = new Solicitud();
        solicitud.setId(10L);
        solicitud.setTrabajador(trabajador);
        solicitud.setEstado(EstadoSolicitud.ACEPTADA);
    }

    @Test
    void iniciar_marcaEnProceso_cuandoElTrabajadorAsignadoLaInicia() {
        when(solicitudRepository.findById(10L)).thenReturn(Optional.of(solicitud));
        when(solicitudRepository.save(any(Solicitud.class))).thenAnswer(inv -> inv.getArgument(0));

        Solicitud resultado = solicitudService.iniciar(10L, trabajador);

        assertEquals(EstadoSolicitud.EN_PROCESO, resultado.getEstado());
    }

    @Test
    void iniciar_lanzaExcepcion_siLaSolicitudNoEsDeEseTrabajador() {
        when(solicitudRepository.findById(10L)).thenReturn(Optional.of(solicitud));
        Usuario otroTrabajador = new Usuario();
        otroTrabajador.setId(99L);

        assertThrows(IllegalArgumentException.class, () -> solicitudService.iniciar(10L, otroTrabajador));
        verify(solicitudRepository, never()).save(any());
    }

    @Test
    void iniciar_lanzaExcepcion_siLaSolicitudNoEstaAceptada() {
        solicitud.setEstado(EstadoSolicitud.EN_PROCESO);
        when(solicitudRepository.findById(10L)).thenReturn(Optional.of(solicitud));

        assertThrows(IllegalStateException.class, () -> solicitudService.iniciar(10L, trabajador));
        verify(solicitudRepository, never()).save(any());
    }
}
