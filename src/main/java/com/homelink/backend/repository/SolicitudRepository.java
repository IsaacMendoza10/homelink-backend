package com.homelink.backend.repository;

import com.homelink.backend.model.EstadoSolicitud;
import com.homelink.backend.model.Solicitud;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface SolicitudRepository extends JpaRepository<Solicitud, Long> {

    List<Solicitud> findByClienteId(Long clienteId);

    List<Solicitud> findByTrabajadorId(Long trabajadorId);

    List<Solicitud> findByCategoriaIdAndEstado(Long categoriaId, EstadoSolicitud estado);

    List<Solicitud> findByFechaCreacionBeforeAndEstadoIn(LocalDateTime limite, List<EstadoSolicitud> estados);
}
