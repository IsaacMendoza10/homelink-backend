package com.homelink.backend.service;

import com.homelink.backend.model.Solicitud;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// Tarea programada que revisa, cada minuto, los plazos de HomeLink:
// - Postulaciones ENVIADA que superaron los 60 minutos sin respuesta del cliente.
// - Solicitudes que superaron las 24 horas desde su creacion sin llegar a un
//   estado final; al expirar, cancela en cascada sus postulaciones pendientes.
// Requiere @EnableScheduling en HomelinkBackendApplication.
@Component
public class SolicitudScheduler {

    private final SolicitudService solicitudService;
    private final PostulacionService postulacionService;

    public SolicitudScheduler(SolicitudService solicitudService, PostulacionService postulacionService) {
        this.solicitudService = solicitudService;
        this.postulacionService = postulacionService;
    }

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void revisarVencimientos() {
        postulacionService.expirarOfertasVencidas();
        List<Solicitud> solicitudesVencidas = solicitudService.expirarVencidas();
        for (Solicitud solicitud : solicitudesVencidas) {
            postulacionService.cancelarPendientesDeSolicitud(solicitud.getId());
        }
    }
}
