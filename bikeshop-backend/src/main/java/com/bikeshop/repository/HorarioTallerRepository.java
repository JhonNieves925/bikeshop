package com.bikeshop.repository;

import com.bikeshop.entity.HorarioTaller;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface HorarioTallerRepository extends JpaRepository<HorarioTaller, Long> {

    List<HorarioTaller> findByActivoTrueOrderByDiaSemanaAsc();

    Optional<HorarioTaller> findByDiaSemana(Byte diaSemana);
}
