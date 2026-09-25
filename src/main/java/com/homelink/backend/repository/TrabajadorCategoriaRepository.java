package com.homelink.backend.repository;

import com.homelink.backend.model.EstadoAprobacion;
import com.homelink.backend.model.TrabajadorCategoria;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TrabajadorCategoriaRepository extends JpaRepository<TrabajadorCategoria, Long> {

    List<TrabajadorCategoria> findByPerfilTrabajadorId(Long perfilTrabajadorId);

    List<TrabajadorCategoria> findByCategoriaIdAndPerfilTrabajadorEstadoAprobacion(Long categoriaId, EstadoAprobacion estado);

    boolean existsByPerfilTrabajadorIdAndCategoriaId(Long perfilTrabajadorId, Long categoriaId);
}
