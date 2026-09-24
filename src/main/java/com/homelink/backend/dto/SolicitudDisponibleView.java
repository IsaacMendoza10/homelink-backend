package com.homelink.backend.dto;

import com.homelink.backend.model.Solicitud;

// Una solicitud abierta tal como la ve un trabajador candidato: cuantos
// postulantes tiene ya (X/7) y cuanto tiempo le queda antes de vencer (24h).
public class SolicitudDisponibleView {

    private final Solicitud solicitud;
    private final int postulantesActuales;
    private final int postulantesMaximo;
    private final String tiempoRestanteTexto;
    private final boolean yaPostulado;

    public SolicitudDisponibleView(Solicitud solicitud, int postulantesActuales, int postulantesMaximo,
                                    String tiempoRestanteTexto, boolean yaPostulado) {
        this.solicitud = solicitud;
        this.postulantesActuales = postulantesActuales;
        this.postulantesMaximo = postulantesMaximo;
        this.tiempoRestanteTexto = tiempoRestanteTexto;
        this.yaPostulado = yaPostulado;
    }

    public Solicitud getSolicitud() {
        return solicitud;
    }

    public int getPostulantesActuales() {
        return postulantesActuales;
    }

    public int getPostulantesMaximo() {
        return postulantesMaximo;
    }

    public String getTiempoRestanteTexto() {
        return tiempoRestanteTexto;
    }

    public boolean isYaPostulado() {
        return yaPostulado;
    }

    public boolean isCupoLleno() {
        return postulantesActuales >= postulantesMaximo;
    }
}
