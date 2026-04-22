package com.bikeshop.repository;

import com.bikeshop.entity.Mantenimiento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface MantenimientoRepository extends JpaRepository<Mantenimiento, Long> {

    @Modifying
    @Query("UPDATE Mantenimiento m SET m.atendidoPor = null WHERE m.atendidoPor.id = :id")
    void clearAtendidoPorByUsuarioId(@Param("id") Long id);

    List<Mantenimiento> findByFechaAndEstadoNot(LocalDate fecha, Mantenimiento.Estado estado);

    List<Mantenimiento> findByFechaOrderByHoraInicioAsc(LocalDate fecha);

    List<Mantenimiento> findByFechaBetweenOrderByFechaAscHoraInicioAsc(LocalDate desde, LocalDate hasta);

    List<Mantenimiento> findByClienteIdOrderByFechaDesc(Long clienteId);

    Page<Mantenimiento> findByEstadoOrderByFechaAsc(Mantenimiento.Estado estado, Pageable pageable);

    List<Mantenimiento> findByBicicletaIdOrderByFechaDesc(Long bicicletaId);
}
