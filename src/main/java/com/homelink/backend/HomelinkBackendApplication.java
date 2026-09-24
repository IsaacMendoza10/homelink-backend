package com.homelink.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

// @EnableScheduling activa el job programado (ver SolicitudScheduler) que
// revisa cada minuto los plazos de solicitudes (24h) y postulaciones (60min).
@SpringBootApplication
@EnableScheduling
public class HomelinkBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(HomelinkBackendApplication.class, args);
    }

}
