package com.bikeshop.repository;

import com.bikeshop.entity.InventarioMovimiento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface InventarioMovimientoRepository extends JpaRepository<InventarioMovimiento, Long> {

    @Modifying
    @Query("UPDATE InventarioMovimiento m SET m.usuario = null WHERE m.usuario.id = :id")
    void clearUsuarioByUsuarioId(@Param("id") Long id);

    @Modifying
    @Query("UPDATE InventarioMovimiento m SET m.producto = null WHERE m.producto.id = :id")
    void clearProductoByProductoId(@Param("id") Long id);

    List<InventarioMovimiento> findByProductoIdOrderByFechaDesc(Long productoId);

    Page<InventarioMovimiento> findAllByOrderByFechaDesc(Pageable pageable);
}
