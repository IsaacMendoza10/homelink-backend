package com.homelink.backend.dto;

import com.homelink.backend.model.Postulacion;

// Una postulacion (propia, recibida como invitacion, o de un postulante ajeno
// que el cliente esta revisando) junto con su tiempo restante ya formateado
// para la vista, y si el trabajador ya puede retirarla (pasados 20 min).
public class PostulacionVista {

    private final Postulacion postulacion;
    private final String tiempoRestanteTexto;
    private final boolean retirable;

    public PostulacionVista(Postulacion postulacion, String tiempoRestanteTexto, boolean retirable) {
        this.postulacion = postulacion;
        this.tiempoRestanteTexto = tiempoRestanteTexto;
        this.retirable = retirable;
    }

    public Postulacion getPostulacion() {
        return postulacion;
    }

    public String getTiempoRestanteTexto() {
        return tiempoRestanteTexto;
    }

    public boolean isRetirable() {
        return retirable;
    }
}
