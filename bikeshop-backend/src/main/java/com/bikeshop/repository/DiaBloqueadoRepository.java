package com.bikeshop.repository;

import com.bikeshop.entity.DiaBloqueado;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DiaBloqueadoRepository extends JpaRepository<DiaBloqueado, Long> {

    void deleteByCreadoPorId(Long id);

    boolean existsByFecha(LocalDate fecha);

    Optional<DiaBloqueado> findByFecha(LocalDate fecha);

    List<DiaBloqueado> findByFechaBetween(LocalDate desde, LocalDate hasta);
}
