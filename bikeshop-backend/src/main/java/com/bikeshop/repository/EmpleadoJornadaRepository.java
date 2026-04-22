package com.bikeshop.repository;

import com.bikeshop.entity.EmpleadoJornada;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface EmpleadoJornadaRepository extends JpaRepository<EmpleadoJornada, Long> {

    Optional<EmpleadoJornada> findByUsuarioIdAndFecha(Long usuarioId, LocalDate fecha);

    List<EmpleadoJornada> findByUsuarioIdAndFechaBetween(Long usuarioId, LocalDate desde, LocalDate hasta);

    void deleteByUsuarioId(Long usuarioId);
}
