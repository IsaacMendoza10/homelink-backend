package com.homelink.backend.repository;

import com.homelink.backend.model.EstadoPostulacion;
import com.homelink.backend.model.OrigenPostulacion;
import com.homelink.backend.model.Postulacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PostulacionRepository extends JpaRepository<Postulacion, Long> {

    List<Postulacion> findBySolicitudId(Long solicitudId);

    List<Postulacion> findBySolicitudIdAndEstado(Long solicitudId, EstadoPostulacion estado);

    Optional<Postulacion> findBySolicitudIdAndTrabajadorIdAndEstado(Long solicitudId, Long trabajadorId, EstadoPostulacion estado);

    List<Postulacion> findByTrabajadorIdAndOrigenAndEstado(Long trabajadorId, OrigenPostulacion origen, EstadoPostulacion estado);

    List<Postulacion> findByEstadoAndFechaEnvioBefore(EstadoPostulacion estado, LocalDateTime limite);
}
