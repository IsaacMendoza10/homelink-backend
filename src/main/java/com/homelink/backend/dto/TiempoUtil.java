package com.homelink.backend.dto;

import java.time.Duration;
import java.time.LocalDateTime;

// Pequena utilidad para mostrar, en las vistas, cuanto tiempo falta hasta una
// fecha limite (por ejemplo, el vencimiento de 24h de una solicitud o el de
// 60 min que tiene el cliente para responder una postulacion).
public final class TiempoUtil {

    private TiempoUtil() {
    }

    public static String formatoRestante(LocalDateTime limite) {
        Duration restante = Duration.between(LocalDateTime.now(), limite);
        if (restante.isNegative()) {
            return "Vencido";
        }
        long horas = restante.toHours();
        long minutos = restante.toMinutesPart();
        if (horas > 0) {
            return horas + "h " + minutos + "min";
        }
        return minutos + "min";
    }
}
