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

    public Optional<Solicitud> buscarPorId(Long id) {
        return solicitudRepository.findById(id);
    }

    public Solicitud aceptar(Long solicitudId, Usuario trabajador) {
        Solicitud solicitud = obtenerOLanzar(solicitudId);
        solicitud.setTrabajador(trabajador);
        solicitud.setEstado(EstadoSolicitud.ACEPTADA);
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

    // NOTA: no se maneja aqui una excepcion propia (ej. SolicitudNoEncontradaException),
    // sino IllegalArgumentException generica. Sin un @ControllerAdvice global, esto
    // se traduce en un error 500 poco descriptivo para quien consuma la app.
    private Solicitud obtenerOLanzar(Long id) {
        return solicitudRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Solicitud no encontrada: " + id));
    }
}
