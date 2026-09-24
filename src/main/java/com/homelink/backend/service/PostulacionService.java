package com.homelink.backend.service;

import com.homelink.backend.model.*;
import com.homelink.backend.repository.PostulacionRepository;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

// Gestiona las postulaciones (ofertas) que conectan una Solicitud con uno o
// varios trabajadores candidatos: tanto las que envia un trabajador a una
// solicitud abierta (origen TRABAJADOR) como las invitaciones directas que
// crea el cliente hacia un trabajador especifico (origen CLIENTE). Ambas
// comparten el mismo ciclo de vida: ENVIADA -> ACEPTADA/RECHAZADA/EXPIRADA/RETIRADA.
@Service
public class PostulacionService {

    public static final int MAX_POSTULANTES = 7;
    public static final long MINUTOS_RESPUESTA_CLIENTE = 60;
    public static final long MINUTOS_RETIRO_TRABAJADOR = 20;

    private final PostulacionRepository postulacionRepository;
    private final SolicitudService solicitudService;

    public PostulacionService(PostulacionRepository postulacionRepository, SolicitudService solicitudService) {
        this.postulacionRepository = postulacionRepository;
        this.solicitudService = solicitudService;
    }

    public int contarActivas(Long solicitudId) {
        return postulacionRepository.findBySolicitudIdAndEstado(solicitudId, EstadoPostulacion.ENVIADA).size();
    }

    public boolean yaPostulado(Long solicitudId, Long trabajadorId) {
        return postulacionRepository
                .findBySolicitudIdAndTrabajadorIdAndEstado(solicitudId, trabajadorId, EstadoPostulacion.ENVIADA)
                .isPresent();
    }

    public List<Postulacion> listarPostulantes(Long solicitudId) {
        return postulacionRepository.findBySolicitudId(solicitudId);
    }

    public List<Postulacion> listarEnviadasPorTrabajador(Long trabajadorId) {
        return postulacionRepository.findByTrabajadorIdAndOrigenAndEstado(
                trabajadorId, OrigenPostulacion.TRABAJADOR, EstadoPostulacion.ENVIADA);
    }

    public List<Postulacion> listarInvitacionesPorTrabajador(Long trabajadorId) {
        return postulacionRepository.findByTrabajadorIdAndOrigenAndEstado(
                trabajadorId, OrigenPostulacion.CLIENTE, EstadoPostulacion.ENVIADA);
    }

    // El trabajador se postula a una solicitud abierta.
    public Postulacion postularse(Solicitud solicitud, Usuario trabajador) {
        if (solicitud.getEstado() != EstadoSolicitud.PENDIENTE) {
            throw new IllegalStateException("Esta solicitud ya no esta disponible.");
        }
        if (contarActivas(solicitud.getId()) >= MAX_POSTULANTES) {
            throw new IllegalStateException("Esta solicitud ya alcanzo el maximo de postulantes.");
        }
        if (yaPostulado(solicitud.getId(), trabajador.getId())) {
            throw new IllegalStateException("Ya tienes una postulacion activa en esta solicitud.");
        }
        return crear(solicitud, trabajador, OrigenPostulacion.TRABAJADOR);
    }

    // El cliente invita directamente a un trabajador especifico, al crear la solicitud.
    public Postulacion invitar(Solicitud solicitud, Usuario trabajador) {
        return crear(solicitud, trabajador, OrigenPostulacion.CLIENTE);
    }

    private Postulacion crear(Solicitud solicitud, Usuario trabajador, OrigenPostulacion origen) {
        Postulacion postulacion = new Postulacion();
        postulacion.setSolicitud(solicitud);
        postulacion.setTrabajador(trabajador);
        postulacion.setOrigen(origen);
        postulacion.setEstado(EstadoPostulacion.ENVIADA);
        postulacion.setFechaEnvio(LocalDateTime.now());
        return postulacionRepository.save(postulacion);
    }

    // El cliente acepta a un trabajador que se postulo por su cuenta a una solicitud abierta.
    public Postulacion aceptar(Long postulacionId, Usuario cliente) {
        Postulacion postulacion = obtenerOLanzar(postulacionId);
        if (!postulacion.getSolicitud().getCliente().getId().equals(cliente.getId())) {
            throw new IllegalArgumentException("No puedes gestionar postulaciones de una solicitud que no es tuya.");
        }
        return aceptarInterno(postulacion);
    }

    // El trabajador acepta una invitacion directa que le envio el cliente.
    public Postulacion aceptarInvitacion(Long postulacionId, Usuario trabajador) {
        Postulacion postulacion = obtenerOLanzar(postulacionId);
        if (!postulacion.getTrabajador().getId().equals(trabajador.getId())) {
            throw new IllegalArgumentException("Esta invitacion no es para ti.");
        }
        return aceptarInterno(postulacion);
    }

    private Postulacion aceptarInterno(Postulacion postulacion) {
        if (postulacion.getEstado() != EstadoPostulacion.ENVIADA) {
            throw new IllegalStateException("Esta postulacion ya fue resuelta.");
        }
        postulacion.setEstado(EstadoPostulacion.ACEPTADA);
        postulacionRepository.save(postulacion);
        solicitudService.aceptar(postulacion.getSolicitud().getId(), postulacion.getTrabajador());
        rechazarRestantes(postulacion.getSolicitud().getId(), postulacion.getId());
        return postulacion;
    }

    // El cliente rechaza a uno de los postulantes (puede seguir revisando a los demas).
    public Postulacion rechazar(Long postulacionId, Usuario cliente) {
        Postulacion postulacion = obtenerOLanzar(postulacionId);
        if (!postulacion.getSolicitud().getCliente().getId().equals(cliente.getId())) {
            throw new IllegalArgumentException("No puedes gestionar postulaciones de una solicitud que no es tuya.");
        }
        return marcarResuelta(postulacion, EstadoPostulacion.RECHAZADA);
    }

    // El trabajador rechaza (declina) una invitacion directa.
    public Postulacion rechazarInvitacion(Long postulacionId, Usuario trabajador) {
        Postulacion postulacion = obtenerOLanzar(postulacionId);
        if (!postulacion.getTrabajador().getId().equals(trabajador.getId())) {
            throw new IllegalArgumentException("Esta invitacion no es para ti.");
        }
        return marcarResuelta(postulacion, EstadoPostulacion.RECHAZADA);
    }

    // El trabajador retira su propia postulacion a una solicitud abierta, una vez
    // pasados al menos 20 minutos desde que la envio.
    public Postulacion retirar(Long postulacionId, Usuario trabajador) {
        Postulacion postulacion = obtenerOLanzar(postulacionId);
        if (!postulacion.getTrabajador().getId().equals(trabajador.getId())) {
            throw new IllegalArgumentException("Esta postulacion no es tuya.");
        }
        if (postulacion.getEstado() != EstadoPostulacion.ENVIADA) {
            throw new IllegalStateException("Esta postulacion ya fue resuelta.");
        }
        long minutosTranscurridos = Duration.between(postulacion.getFechaEnvio(), LocalDateTime.now()).toMinutes();
        if (minutosTranscurridos < MINUTOS_RETIRO_TRABAJADOR) {
            throw new IllegalStateException("Debes esperar al menos 20 minutos antes de retirar tu postulacion.");
        }
        return marcarResuelta(postulacion, EstadoPostulacion.RETIRADA);
    }

    private Postulacion marcarResuelta(Postulacion postulacion, EstadoPostulacion nuevoEstado) {
        if (postulacion.getEstado() != EstadoPostulacion.ENVIADA) {
            throw new IllegalStateException("Esta postulacion ya fue resuelta.");
        }
        postulacion.setEstado(nuevoEstado);
        return postulacionRepository.save(postulacion);
    }

    private void rechazarRestantes(Long solicitudId, Long postulacionAceptadaId) {
        List<Postulacion> otras = postulacionRepository.findBySolicitudIdAndEstado(solicitudId, EstadoPostulacion.ENVIADA);
        for (Postulacion otra : otras) {
            if (!otra.getId().equals(postulacionAceptadaId)) {
                otra.setEstado(EstadoPostulacion.RECHAZADA);
                postulacionRepository.save(otra);
            }
        }
    }

    // Usado por el job programado (SolicitudScheduler): expira ofertas que
    // superaron los 60 minutos sin respuesta del cliente.
    public void expirarOfertasVencidas() {
        LocalDateTime limite = LocalDateTime.now().minusMinutes(MINUTOS_RESPUESTA_CLIENTE);
        List<Postulacion> vencidas = postulacionRepository.findByEstadoAndFechaEnvioBefore(EstadoPostulacion.ENVIADA, limite);
        for (Postulacion p : vencidas) {
            p.setEstado(EstadoPostulacion.EXPIRADA);
            postulacionRepository.save(p);
        }
    }

    // Usado cuando una solicitud se cierra sin resolver (vencio a las 24h, o el
    // cliente la cancelo manualmente): cancela en cascada las postulaciones que
    // seguian sin respuesta, para que ningun trabajador quede esperando una
    // oferta que ya no tiene sentido.
    public void cancelarPendientesDeSolicitud(Long solicitudId) {
        List<Postulacion> pendientes = postulacionRepository.findBySolicitudIdAndEstado(solicitudId, EstadoPostulacion.ENVIADA);
        for (Postulacion p : pendientes) {
            p.setEstado(EstadoPostulacion.EXPIRADA);
            postulacionRepository.save(p);
        }
    }

    private Postulacion obtenerOLanzar(Long id) {
        return postulacionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Postulacion no encontrada: " + id));
    }
}
