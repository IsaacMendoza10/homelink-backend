package com.homelink.backend.service;

import com.homelink.backend.model.*;
import com.homelink.backend.repository.CalificacionRepository;
import com.homelink.backend.repository.SolicitudRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class SolicitudService {

    private final SolicitudRepository solicitudRepository;
    private final CalificacionRepository calificacionRepository;

    public SolicitudService(SolicitudRepository solicitudRepository, CalificacionRepository calificacionRepository) {
        this.solicitudRepository = solicitudRepository;
        this.calificacionRepository = calificacionRepository;
    }

    public Solicitud crear(Usuario cliente, Categoria categoria, String descripcion, String direccion) {
        Solicitud solicitud = new Solicitud();
        solicitud.setCliente(cliente);
        solicitud.setCategoria(categoria);
        solicitud.setDescripcion(descripcion);
        solicitud.setDireccion(direccion);
        solicitud.setEstado(EstadoSolicitud.PENDIENTE);
        return solicitudRepository.save(solicitud);
    }

    public List<Solicitud> listarPorCliente(Long clienteId) {
        return solicitudRepository.findByClienteId(clienteId);
    }

    public List<Solicitud> listarPorTrabajador(Long trabajadorId) {
        return solicitudRepository.findByTrabajadorId(trabajadorId);
    }

    // Solicitudes abiertas (sin trabajador asignado todavia) dentro de una categoria,
    // para que un trabajador aprobado en esa categoria pueda verlas y postularse.
    public List<Solicitud> listarAbiertasPorCategoria(Long categoriaId) {
        return solicitudRepository.findByCategoriaIdAndEstado(categoriaId, EstadoSolicitud.PENDIENTE);
    }

    public Optional<Solicitud> buscarPorId(Long id) {
        return solicitudRepository.findById(id);
    }

    public Solicitud aceptar(Long solicitudId, Usuario trabajador) {
        Solicitud solicitud = obtenerOLanzar(solicitudId);
        solicitud.setTrabajador(trabajador);
        solicitud.setEstado(EstadoSolicitud.ACEPTADA);
        return solicitudRepository.save(solicitud);
    }

    // El trabajador asignado marca que ya empezo el servicio (ACEPTADA -> EN_PROCESO).
    // Antes de esto no habia forma de distinguir "ya voy a ir" de "ya termine": el
    // unico paso disponible tras aceptar era Finalizar directamente.
    public Solicitud iniciar(Long solicitudId, Usuario trabajador) {
        Solicitud solicitud = obtenerOLanzar(solicitudId);
        if (solicitud.getTrabajador() == null || !solicitud.getTrabajador().getId().equals(trabajador.getId())) {
            throw new IllegalArgumentException("Esta solicitud no te fue asignada.");
        }
        if (solicitud.getEstado() != EstadoSolicitud.ACEPTADA) {
            throw new IllegalStateException("Esta solicitud no esta lista para iniciar.");
        }
        solicitud.setEstado(EstadoSolicitud.EN_PROCESO);
        return solicitudRepository.save(solicitud);
    }

    public Solicitud finalizar(Long solicitudId) {
        Solicitud solicitud = obtenerOLanzar(solicitudId);
        solicitud.setEstado(EstadoSolicitud.FINALIZADA);
        solicitud.setFechaFinalizacion(LocalDateTime.now());
        return solicitudRepository.save(solicitud);
    }

    public Solicitud cancelar(Long solicitudId) {
        Solicitud solicitud = obtenerOLanzar(solicitudId);
        solicitud.setEstado(EstadoSolicitud.CANCELADA);
        return solicitudRepository.save(solicitud);
    }

    public Calificacion calificar(Long solicitudId, int puntuacion, String comentario) {
        Solicitud solicitud = obtenerOLanzar(solicitudId);
        Calificacion calificacion = new Calificacion();
        calificacion.setSolicitud(solicitud);
        calificacion.setPuntuacion(puntuacion);
        calificacion.setComentario(comentario);
        return calificacionRepository.save(calificacion);
    }

    // Solicitudes que llevan mas de 24 horas sin llegar a un estado final: se marcan como
    // EXPIRADA para que el flujo de trabajo no se quede estancado. Devuelve las que se
    // acaban de expirar para que el llamador (ver SolicitudScheduler) tambien cancele en
    // cascada sus postulaciones pendientes.
    public List<Solicitud> expirarVencidas() {
        LocalDateTime limite = LocalDateTime.now().minusHours(24);
        List<EstadoSolicitud> activos = List.of(EstadoSolicitud.PENDIENTE, EstadoSolicitud.ACEPTADA, EstadoSolicitud.EN_PROCESO);
        List<Solicitud> vencidas = solicitudRepository.findByFechaCreacionBeforeAndEstadoIn(limite, activos);
        for (Solicitud s : vencidas) {
            s.setEstado(EstadoSolicitud.EXPIRADA);
            solicitudRepository.save(s);
        }
        return vencidas;
    }

    // NOTA: no se maneja aqui una excepcion propia (ej. SolicitudNoEncontradaException),
    // sino IllegalArgumentException generica. Sin un @ControllerAdvice global, esto
    // se traduce en un error 500 poco descriptivo para quien consuma la app.
    private Solicitud obtenerOLanzar(Long id) {
        return solicitudRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Solicitud no encontrada: " + id));
    }
}
