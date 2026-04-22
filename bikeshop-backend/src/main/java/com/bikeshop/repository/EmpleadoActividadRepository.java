package com.bikeshop.repository;

import com.bikeshop.entity.EmpleadoActividad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface EmpleadoActividadRepository extends JpaRepository<EmpleadoActividad, Long> {

    @Modifying
    @Query("UPDATE EmpleadoActividad a SET a.registradoPor = null WHERE a.registradoPor.id = :id")
    void clearRegistradoPorByUsuarioId(@Param("id") Long id);

    List<EmpleadoActividad> findByUsuarioIdAndFechaBetween(Long usuarioId, LocalDate desde, LocalDate hasta);

    List<EmpleadoActividad> findByUsuarioIdOrderByFechaDesc(Long usuarioId);

    void deleteByUsuarioId(Long usuarioId);
}
