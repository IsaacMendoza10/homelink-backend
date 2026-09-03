package com.homelink.backend.repository;

import com.homelink.backend.model.Solicitud;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SolicitudRepository extends JpaRepository<Solicitud, Long> {

    List<Solicitud> findByClienteId(Long clienteId);

    List<Solicitud> findByTrabajadorId(Long trabajadorId);
}
