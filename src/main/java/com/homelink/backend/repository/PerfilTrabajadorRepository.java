package com.homelink.backend.repository;

import com.homelink.backend.model.EstadoAprobacion;
import com.homelink.backend.model.PerfilTrabajador;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PerfilTrabajadorRepository extends JpaRepository<PerfilTrabajador, Long> {

    Optional<PerfilTrabajador> findByUsuarioId(Long usuarioId);

    List<PerfilTrabajador> findByEstadoAprobacion(EstadoAprobacion estado);

    List<PerfilTrabajador> findByCategoriaIdAndEstadoAprobacion(Long categoriaId, EstadoAprobacion estado);
}
