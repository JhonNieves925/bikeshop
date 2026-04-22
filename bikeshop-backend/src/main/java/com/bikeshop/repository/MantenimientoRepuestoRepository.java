package com.bikeshop.repository;

import com.bikeshop.entity.MantenimientoRepuesto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MantenimientoRepuestoRepository extends JpaRepository<MantenimientoRepuesto, Long> {

    List<MantenimientoRepuesto> findByMantenimientoId(Long mantenimientoId);

    @Modifying
    @Query("UPDATE MantenimientoRepuesto r SET r.producto = null WHERE r.producto.id = :id")
    void clearProductoByProductoId(@Param("id") Long id);
}
