package com.homelink.backend.repository;

import com.homelink.backend.model.Calificacion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CalificacionRepository extends JpaRepository<Calificacion, Long> {
}
