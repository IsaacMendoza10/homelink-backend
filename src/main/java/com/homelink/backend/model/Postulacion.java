package com.homelink.backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

// Conecta una Solicitud con un trabajador candidato: puede haberla creado el
// propio trabajador al postularse a una solicitud abierta (origen TRABAJADOR),
// o el cliente al invitar directamente a un trabajador especifico (origen
// CLIENTE). Maximo 7 postulaciones activas por solicitud, y limites de tiempo
// para responder/retirarse (ver PostulacionService).
@Entity
@Table(name = "postulaciones")
public class Postulacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "solicitud_id", nullable = false)
    private Solicitud solicitud;

    @ManyToOne
    @JoinColumn(name = "trabajador_id", nullable = false)
    private Usuario trabajador;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrigenPostulacion origen;

    @Enumerated(EnumType.STRING)
    private EstadoPostulacion estado = EstadoPostulacion.ENVIADA;

    private LocalDateTime fechaEnvio = LocalDateTime.now();

    public Postulacion() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Solicitud getSolicitud() {
        return solicitud;
    }

    public void setSolicitud(Solicitud solicitud) {
        this.solicitud = solicitud;
    }

    public Usuario getTrabajador() {
        return trabajador;
    }

    public void setTrabajador(Usuario trabajador) {
        this.trabajador = trabajador;
    }

    public OrigenPostulacion getOrigen() {
        return origen;
    }

    public void setOrigen(OrigenPostulacion origen) {
        this.origen = origen;
    }

    public EstadoPostulacion getEstado() {
        return estado;
    }

    public void setEstado(EstadoPostulacion estado) {
        this.estado = estado;
    }

    public LocalDateTime getFechaEnvio() {
        return fechaEnvio;
    }

    public void setFechaEnvio(LocalDateTime fechaEnvio) {
        this.fechaEnvio = fechaEnvio;
    }
}
