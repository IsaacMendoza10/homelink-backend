package com.homelink.backend.service;

import com.homelink.backend.model.*;
import com.homelink.backend.repository.PostulacionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

// Pruebas unitarias de las reglas de negocio de PostulacionService: tope de
// postulantes (7), quien puede aceptar/rechazar segun quien inicio la
// postulacion, el rechazo en cascada al aceptar a un postulante, y los plazos
// de 60 minutos (respuesta del cliente) y 20 minutos (retiro del trabajador).
// No se usa @SpringBootTest: son pruebas unitarias puras con Mockito, mas
// rapidas y suficientes porque PostulacionService no depende del contexto de
// Spring, solo de sus dos colaboradores (PostulacionRepository, SolicitudService).
@ExtendWith(MockitoExtension.class)
class PostulacionServiceTest {

    @Mock
    private PostulacionRepository postulacionRepository;

    @Mock
    private SolicitudService solicitudService;

    private PostulacionService postulacionService;

    private Usuario cliente;
    private Usuario trabajador;
    private Solicitud solicitud;

    @BeforeEach
    void setUp() {
        postulacionService = new PostulacionService(postulacionRepository, solicitudService);

        cliente = new Usuario();
        cliente.setId(1L);
        cliente.setRol(RolUsuario.CLIENTE);

        trabajador = new Usuario();
        trabajador.setId(2L);
        trabajador.setRol(RolUsuario.TRABAJADOR);

        solicitud = new Solicitud();
        solicitud.setId(10L);
        solicitud.setCliente(cliente);
        solicitud.setEstado(EstadoSolicitud.PENDIENTE);
    }

    @Test
    void postularse_creaLaPostulacionCuandoHayCupoYNoSeHaPostuladoAntes() {
        when(postulacionRepository.findBySolicitudIdAndEstado(10L, EstadoPostulacion.ENVIADA))
                .thenReturn(List.of());
        when(postulacionRepository.findBySolicitudIdAndTrabajadorIdAndEstado(10L, 2L, EstadoPostulacion.ENVIADA))
                .thenReturn(Optional.empty());
        when(postulacionRepository.save(any(Postulacion.class))).thenAnswer(inv -> inv.getArgument(0));

        Postulacion resultado = postulacionService.postularse(solicitud, trabajador);

        assertEquals(EstadoPostulacion.ENVIADA, resultado.getEstado());
        assertEquals(OrigenPostulacion.TRABAJADOR, resultado.getOrigen());
        assertEquals(trabajador, resultado.getTrabajador());
    }

    @Test
    void postularse_lanzaExcepcion_siLaSolicitudYaNoEstaPendiente() {
        solicitud.setEstado(EstadoSolicitud.ACEPTADA);

        assertThrows(IllegalStateException.class, () -> postulacionService.postularse(solicitud, trabajador));
        verify(postulacionRepository, never()).save(any());
    }

    @Test
    void postularse_lanzaExcepcion_siYaHaySietePostulantesActivos() {
        List<Postulacion> sietePostulantes = List.of(
                new Postulacion(), new Postulacion(), new Postulacion(), new Postulacion(),
                new Postulacion(), new Postulacion(), new Postulacion());
        when(postulacionRepository.findBySolicitudIdAndEstado(10L, EstadoPostulacion.ENVIADA))
                .thenReturn(sietePostulantes);

        assertThrows(IllegalStateException.class, () -> postulacionService.postularse(solicitud, trabajador));
        verify(postulacionRepository, never()).save(any());
    }

    @Test
    void postularse_lanzaExcepcion_siElTrabajadorYaTieneUnaPostulacionActivaAhi() {
        when(postulacionRepository.findBySolicitudIdAndEstado(10L, EstadoPostulacion.ENVIADA))
                .thenReturn(List.of());
        when(postulacionRepository.findBySolicitudIdAndTrabajadorIdAndEstado(10L, 2L, EstadoPostulacion.ENVIADA))
                .thenReturn(Optional.of(new Postulacion()));

        assertThrows(IllegalStateException.class, () -> postulacionService.postularse(solicitud, trabajador));
        verify(postulacionRepository, never()).save(any());
    }

    @Test
    void aceptar_marcaAceptadaYRechazaLasDemasPostulacionesEnviadas() {
        Postulacion aAceptar = new Postulacion();
        aAceptar.setId(100L);
        aAceptar.setSolicitud(solicitud);
        aAceptar.setTrabajador(trabajador);
        aAceptar.setEstado(EstadoPostulacion.ENVIADA);

        Usuario otroTrabajador = new Usuario();
        otroTrabajador.setId(3L);
        Postulacion otra = new Postulacion();
        otra.setId(101L);
        otra.setSolicitud(solicitud);
        otra.setTrabajador(otroTrabajador);
        otra.setEstado(EstadoPostulacion.ENVIADA);

        when(postulacionRepository.findById(100L)).thenReturn(Optional.of(aAceptar));
        when(postulacionRepository.save(any(Postulacion.class))).thenAnswer(inv -> inv.getArgument(0));
        when(postulacionRepository.findBySolicitudIdAndEstado(10L, EstadoPostulacion.ENVIADA))
                .thenReturn(List.of(aAceptar, otra));

        postulacionService.aceptar(100L, cliente);

        assertEquals(EstadoPostulacion.ACEPTADA, aAceptar.getEstado());
        assertEquals(EstadoPostulacion.RECHAZADA, otra.getEstado());
        verify(solicitudService).aceptar(10L, trabajador);
    }

    @Test
    void aceptar_lanzaExcepcion_siQuienAceptaNoEsElClienteDeLaSolicitud() {
        Postulacion postulacion = new Postulacion();
        postulacion.setId(100L);
        postulacion.setSolicitud(solicitud);
        postulacion.setTrabajador(trabajador);
        postulacion.setEstado(EstadoPostulacion.ENVIADA);
        when(postulacionRepository.findById(100L)).thenReturn(Optional.of(postulacion));

        Usuario otroCliente = new Usuario();
        otroCliente.setId(99L);

        assertThrows(IllegalArgumentException.class, () -> postulacionService.aceptar(100L, otroCliente));
        verify(solicitudService, never()).aceptar(any(), any());
    }

    @Test
    void retirar_lanzaExcepcion_siAunNoPasanVeinteMinutos() {
        Postulacion postulacion = new Postulacion();
        postulacion.setId(100L);
        postulacion.setTrabajador(trabajador);
        postulacion.setEstado(EstadoPostulacion.ENVIADA);
        postulacion.setFechaEnvio(LocalDateTime.now().minusMinutes(5));
        when(postulacionRepository.findById(100L)).thenReturn(Optional.of(postulacion));

        assertThrows(IllegalStateException.class, () -> postulacionService.retirar(100L, trabajador));
        verify(postulacionRepository, never()).save(any());
    }

    @Test
    void retirar_marcaRetirada_siYaPasaronVeinteMinutos() {
        Postulacion postulacion = new Postulacion();
        postulacion.setId(100L);
        postulacion.setTrabajador(trabajador);
        postulacion.setEstado(EstadoPostulacion.ENVIADA);
        postulacion.setFechaEnvio(LocalDateTime.now().minusMinutes(25));
        when(postulacionRepository.findById(100L)).thenReturn(Optional.of(postulacion));
        when(postulacionRepository.save(any(Postulacion.class))).thenAnswer(inv -> inv.getArgument(0));

        Postulacion resultado = postulacionService.retirar(100L, trabajador);

        assertEquals(EstadoPostulacion.RETIRADA, resultado.getEstado());
    }

    @Test
    void expirarOfertasVencidas_marcaExpiradaLasQueSuperanLosSesentaMinutos() {
        Postulacion vencida = new Postulacion();
        vencida.setId(200L);
        vencida.setEstado(EstadoPostulacion.ENVIADA);
        when(postulacionRepository.findByEstadoAndFechaEnvioBefore(eq(EstadoPostulacion.ENVIADA), any(LocalDateTime.class)))
                .thenReturn(List.of(vencida));
        when(postulacionRepository.save(any(Postulacion.class))).thenAnswer(inv -> inv.getArgument(0));

        postulacionService.expirarOfertasVencidas();

        assertEquals(EstadoPostulacion.EXPIRADA, vencida.getEstado());
    }
}
